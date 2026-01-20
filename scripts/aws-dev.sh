#!/bin/bash
# J15 Backend 開発環境の起動/停止スクリプト
# 使い方: ./scripts/aws-dev.sh start|stop|status

set -e

# AWS CLIのページャーを無効化
export AWS_PAGER=""

REGION="ap-northeast-1"
CLUSTER="j15-backend-cluster-dev"
SERVICE="j15-backend-service-dev"
TASK_FAMILY="j15-backend-dev"
DB_INSTANCE="j15-backend-db-dev"
API_GATEWAY_ID="zu9mkxoir4"
JUDGE_EC2_ID="i-0f91177046e1c651d"
JUDGE_PRIVATE_IP="10.0.1.33"
AWS_S3_BUCKET_NAME="j15-backend-images"

case "$1" in
  start)
    echo "🚀 開発環境を起動中..."
    
    # Judge Service (EC2) 起動
    echo "  Judge Service (EC2) 起動中..."
    aws ec2 start-instances --instance-ids $JUDGE_EC2_ID --region $REGION > /dev/null 2>&1 || true
    
    # RDS起動
    echo "  RDS起動中..."
    aws rds start-db-instance --db-instance-identifier $DB_INSTANCE --region $REGION > /dev/null 2>&1 || true
    
    # RDSが利用可能になるまで待機
    echo "  RDS起動待機中..."
    aws rds wait db-instance-available --db-instance-identifier $DB_INSTANCE --region $REGION
    
    # EC2が起動するまで待機
    echo "  Judge Service起動待機中..."
    aws ec2 wait instance-running --instance-ids $JUDGE_EC2_ID --region $REGION
    
    # ECSタスク定義を更新（環境変数含む）
    echo "  ECSタスク定義更新中..."
    aws ecs describe-task-definition \
      --task-definition $TASK_FAMILY \
      --region $REGION \
      --query 'taskDefinition' > /tmp/current-task-def.json
    
    # 環境変数を更新してタスク定義を登録
    jq --arg AWS_REGION "$REGION" \
       --arg AWS_S3_BUCKET_NAME "$AWS_S3_BUCKET_NAME" \
       --arg JUDGE_SERVICE_BASE_URL "http://${JUDGE_PRIVATE_IP}:8081" \
       '.containerDefinitions[0].environment = (
         (.containerDefinitions[0].environment // []) |
         map(select(.name != "AWS_REGION" and .name != "AWS_S3_BUCKET_NAME" and .name != "JUDGE_SERVICE_BASE_URL")) +
         [
           {name: "AWS_REGION", value: $AWS_REGION},
           {name: "AWS_S3_BUCKET_NAME", value: $AWS_S3_BUCKET_NAME},
           {name: "JUDGE_SERVICE_BASE_URL", value: $JUDGE_SERVICE_BASE_URL}
         ]
       ) |
       del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities, .registeredAt, .registeredBy)' \
      /tmp/current-task-def.json > /tmp/new-task-def.json
    
    NEW_TASK_DEF=$(aws ecs register-task-definition \
      --cli-input-json file:///tmp/new-task-def.json \
      --region $REGION \
      --query 'taskDefinition.taskDefinitionArn' \
      --output text)
    
    echo "  新しいタスク定義: $NEW_TASK_DEF"
    
    # ECS起動（新しいタスク定義で）
    echo "  ECS起動中..."
    aws ecs update-service \
      --cluster $CLUSTER \
      --service $SERVICE \
      --task-definition $NEW_TASK_DEF \
      --desired-count 1 \
      --force-new-deployment \
      --region $REGION > /dev/null
    
    # 新しいタスクが起動するまで待機
    echo "  ECSタスク起動待機中..."
    for i in {1..60}; do
      TASK_ARNS=$(aws ecs list-tasks \
        --cluster $CLUSTER \
        --service-name $SERVICE \
        --desired-status RUNNING \
        --region $REGION \
        --query 'taskArns' \
        --output text)
      
      for TASK_ARN in $TASK_ARNS; do
        TASK_INFO=$(aws ecs describe-tasks \
          --cluster $CLUSTER \
          --tasks $TASK_ARN \
          --region $REGION \
          --query 'tasks[0].{def:taskDefinitionArn,status:lastStatus}' \
          --output json)
        
        TASK_DEF=$(echo $TASK_INFO | jq -r '.def')
        TASK_STATUS=$(echo $TASK_INFO | jq -r '.status')
        
        if [ "$TASK_DEF" == "$NEW_TASK_DEF" ] && [ "$TASK_STATUS" == "RUNNING" ]; then
          ENI_ID=$(aws ecs describe-tasks \
            --cluster $CLUSTER \
            --tasks $TASK_ARN \
            --region $REGION \
            --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' \
            --output text)
          NEW_IP=$(aws ec2 describe-network-interfaces \
            --network-interface-ids $ENI_ID \
            --region $REGION \
            --query 'NetworkInterfaces[0].Association.PublicIp' \
            --output text)
          if [ -n "$NEW_IP" ] && [ "$NEW_IP" != "None" ]; then
            echo "  新しいタスクIP: $NEW_IP"
            
            # API Gateway更新
            echo "  API Gateway更新中..."
            INTEGRATION_ID=$(aws apigatewayv2 get-integrations \
              --api-id $API_GATEWAY_ID \
              --region $REGION \
              --query 'Items[0].IntegrationId' \
              --output text)
            aws apigatewayv2 update-integration \
              --api-id $API_GATEWAY_ID \
              --integration-id $INTEGRATION_ID \
              --integration-uri "http://${NEW_IP}:8080/{proxy}" \
              --region $REGION > /dev/null
            
            # Judge ServiceのパブリックIP取得
            JUDGE_IP=$(aws ec2 describe-instances \
              --instance-ids $JUDGE_EC2_ID \
              --region $REGION \
              --query 'Reservations[0].Instances[0].PublicIpAddress' \
              --output text)
            
            echo ""
            echo "✅ 起動完了!"
            echo "   Main API: https://${API_GATEWAY_ID}.execute-api.${REGION}.amazonaws.com"
            echo "   Judge Service: http://${JUDGE_IP}:8081"
            rm -f /tmp/current-task-def.json /tmp/new-task-def.json
            exit 0
          fi
        fi
      done
      
      echo "  タスク起動待機中... ($i/60)"
      sleep 10
    done
    
    echo "❌ タイムアウト: タスクが起動しませんでした"
    rm -f /tmp/current-task-def.json /tmp/new-task-def.json
    exit 1
    ;;
    
  stop)
    echo "🛑 開発環境を停止中..."
    
    # ECS停止
    echo "  ECS停止中..."
    aws ecs update-service --cluster $CLUSTER --service $SERVICE --desired-count 0 --region $REGION > /dev/null
    
    # RDS停止
    echo "  RDS停止中..."
    aws rds stop-db-instance --db-instance-identifier $DB_INSTANCE --region $REGION > /dev/null 2>&1 || true
    
    # Judge Service (EC2) 停止
    echo "  Judge Service (EC2) 停止中..."
    aws ec2 stop-instances --instance-ids $JUDGE_EC2_ID --region $REGION > /dev/null 2>&1 || true
    
    echo ""
    echo "✅ 停止完了!"
    ;;
    
  status)
    echo "📊 開発環境の状態:"
    echo ""
    
    # RDS状態
    RDS_STATUS=$(aws rds describe-db-instances --db-instance-identifier $DB_INSTANCE --region $REGION --query 'DBInstances[0].DBInstanceStatus' --output text 2>/dev/null || echo "not found")
    echo "  RDS: $RDS_STATUS"
    
    # ECS状態
    ECS_COUNT=$(aws ecs describe-services --cluster $CLUSTER --services $SERVICE --region $REGION --query 'services[0].runningCount' --output text 2>/dev/null || echo "0")
    echo "  ECS: $ECS_COUNT タスク稼働中"
    
    # Judge Service (EC2) 状態
    JUDGE_STATE=$(aws ec2 describe-instances --instance-ids $JUDGE_EC2_ID --region $REGION --query 'Reservations[0].Instances[0].State.Name' --output text 2>/dev/null || echo "not found")
    JUDGE_IP=$(aws ec2 describe-instances --instance-ids $JUDGE_EC2_ID --region $REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text 2>/dev/null || echo "N/A")
    echo "  Judge Service: $JUDGE_STATE (IP: $JUDGE_IP)"
    
    # 現在のタスク定義の環境変数確認
    if [ "$ECS_COUNT" -ge 1 ]; then
      TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER --region $REGION --query 'taskArns[0]' --output text 2>/dev/null)
      if [ -n "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
        TASK_DEF=$(aws ecs describe-tasks --cluster $CLUSTER --tasks $TASK_ARN --region $REGION --query 'tasks[0].taskDefinitionArn' --output text 2>/dev/null)
        JUDGE_URL=$(aws ecs describe-task-definition --task-definition $TASK_DEF --region $REGION --query 'taskDefinition.containerDefinitions[0].environment[?name==`JUDGE_SERVICE_BASE_URL`].value' --output text 2>/dev/null)
        echo "  JUDGE_SERVICE_BASE_URL: $JUDGE_URL"
      fi
    fi
    
    echo ""
    if [ "$ECS_COUNT" -ge 1 ]; then
      echo "  Main API: https://${API_GATEWAY_ID}.execute-api.${REGION}.amazonaws.com"
    fi
    if [ "$JUDGE_STATE" == "running" ] && [ "$JUDGE_IP" != "None" ]; then
      echo "  Judge API: http://${JUDGE_IP}:8081"
    fi
    ;;
    
  *)
    echo "使い方: $0 {start|stop|status}"
    exit 1
    ;;
esac
