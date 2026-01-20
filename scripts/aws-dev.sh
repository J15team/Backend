#!/bin/bash
# J15 Backend 開発環境の起動/停止スクリプト
# 使い方: ./scripts/aws-dev.sh start|stop|status

set -e

# AWS CLIのページャーを無効化
export AWS_PAGER=""

REGION="ap-northeast-1"
CLUSTER="j15-backend-cluster-dev"
SERVICE="j15-backend-service-dev"
DB_INSTANCE="j15-backend-db-dev"
API_GATEWAY_ID="zu9mkxoir4"
JUDGE_EC2_ID="i-0f91177046e1c651d"

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
    
    # ECS起動
    echo "  ECS起動中..."
    aws ecs update-service --cluster $CLUSTER --service $SERVICE --desired-count 1 --region $REGION > /dev/null
    
    # タスク起動待機
    echo "  ECSタスク起動待機中..."
    for i in {1..30}; do
      RUNNING=$(aws ecs describe-services --cluster $CLUSTER --services $SERVICE --region $REGION --query 'services[0].runningCount' --output text)
      if [ "$RUNNING" -ge 1 ]; then
        break
      fi
      sleep 10
    done
    
    # API Gateway IP更新
    echo "  API Gateway更新中..."
    TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER --region $REGION --query 'taskArns[0]' --output text)
    ENI_ID=$(aws ecs describe-tasks --cluster $CLUSTER --tasks $TASK_ARN --region $REGION --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text)
    NEW_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID --region $REGION --query 'NetworkInterfaces[0].Association.PublicIp' --output text)
    INTEGRATION_ID=$(aws apigatewayv2 get-integrations --api-id $API_GATEWAY_ID --region $REGION --query 'Items[0].IntegrationId' --output text)
    aws apigatewayv2 update-integration --api-id $API_GATEWAY_ID --integration-id $INTEGRATION_ID --integration-uri "http://${NEW_IP}:8080/{proxy}" --region $REGION > /dev/null
    
    # Judge ServiceのパブリックIP取得
    JUDGE_IP=$(aws ec2 describe-instances --instance-ids $JUDGE_EC2_ID --region $REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
    
    echo ""
    echo "✅ 起動完了!"
    echo "   Main API: https://${API_GATEWAY_ID}.execute-api.${REGION}.amazonaws.com"
    echo "   Judge Service: http://${JUDGE_IP}:8081"
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
    
    echo ""
    # API URL
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
