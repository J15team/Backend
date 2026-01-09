#!/bin/bash
# J15 Backend 開発環境の起動/停止スクリプト
# 使い方: ./scripts/aws-dev.sh start|stop|status

set -e

# AWS CLIのページャーを無効化（非対話環境での実行を安定させる）
export AWS_PAGER=""

CLUSTER="j15-backend-cluster-dev"
SERVICE="j15-backend-service-dev"
DB_INSTANCE="j15-backend-db-dev"
API_GATEWAY_ID="zu9mkxoir4"

case "$1" in
  start)
    echo "🚀 開発環境を起動中..."
    
    # RDS起動
    echo "  RDS起動中..."
    aws rds start-db-instance --db-instance-identifier $DB_INSTANCE > /dev/null 2>&1 || true
    
    # RDSが利用可能になるまで待機
    echo "  RDS起動待機中..."
    aws rds wait db-instance-available --db-instance-identifier $DB_INSTANCE
    
    # ECS起動
    echo "  ECS起動中..."
    aws ecs update-service --cluster $CLUSTER --service $SERVICE --desired-count 1 > /dev/null
    
    # タスク起動待機
    echo "  タスク起動待機中..."
    for i in {1..30}; do
      RUNNING=$(aws ecs describe-services --cluster $CLUSTER --services $SERVICE --query 'services[0].runningCount' --output text)
      if [ "$RUNNING" -ge 1 ]; then
        break
      fi
      sleep 10
    done
    
    # API Gateway IP更新
    echo "  API Gateway更新中..."
    TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER --query 'taskArns[0]' --output text)
    ENI_ID=$(aws ecs describe-tasks --cluster $CLUSTER --tasks $TASK_ARN --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text)
    NEW_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID --query 'NetworkInterfaces[0].Association.PublicIp' --output text)
    INTEGRATION_ID=$(aws apigatewayv2 get-integrations --api-id $API_GATEWAY_ID --query 'Items[0].IntegrationId' --output text)
    aws apigatewayv2 update-integration --api-id $API_GATEWAY_ID --integration-id $INTEGRATION_ID --integration-uri "http://${NEW_IP}:8080/{proxy}" > /dev/null
    
    echo "✅ 起動完了!"
    echo "   API: https://${API_GATEWAY_ID}.execute-api.ap-northeast-1.amazonaws.com"
    ;;
    
  stop)
    echo "🛑 開発環境を停止中..."
    
    # ECS停止
    echo "  ECS停止中..."
    aws ecs update-service --cluster $CLUSTER --service $SERVICE --desired-count 0 > /dev/null
    
    # RDS停止
    echo "  RDS停止中..."
    aws rds stop-db-instance --db-instance-identifier $DB_INSTANCE > /dev/null 2>&1 || true
    
    echo "✅ 停止完了!"
    ;;
    
  status)
    echo "📊 開発環境の状態:"
    
    # RDS状態
    RDS_STATUS=$(aws rds describe-db-instances --db-instance-identifier $DB_INSTANCE --query 'DBInstances[0].DBInstanceStatus' --output text 2>/dev/null || echo "not found")
    echo "  RDS: $RDS_STATUS"
    
    # ECS状態
    ECS_COUNT=$(aws ecs describe-services --cluster $CLUSTER --services $SERVICE --query 'services[0].runningCount' --output text 2>/dev/null || echo "0")
    echo "  ECS: $ECS_COUNT タスク稼働中"
    
    # API URL
    if [ "$ECS_COUNT" -ge 1 ]; then
      echo "  API: https://${API_GATEWAY_ID}.execute-api.ap-northeast-1.amazonaws.com"
    fi
    ;;
    
  *)
    echo "使い方: $0 {start|stop|status}"
    exit 1
    ;;
esac
