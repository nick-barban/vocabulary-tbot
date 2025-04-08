# VocabularyTBot

A Telegram bot that helps users learn vocabulary by sending them words from their Google Translate favorites on a schedule.

## Features

- Integrates with Google Translate favorites
- Sends vocabulary words on a schedule
- Stores word history and progress in DynamoDB
- Deployed as AWS Lambda function
- Infrastructure defined using AWS CDK

## Prerequisites

- Java 17 or later
- Maven
- AWS CLI configured with appropriate credentials
- AWS CDK CLI
- Telegram Bot Token (from [@BotFather](https://t.me/botfather))
- Google Cloud Project with Translate API enabled
- Google OAuth 2.0 credentials

## Environment Variables

The following environment variables need to be set:

- `TELEGRAM_BOT_TOKEN`: Your Telegram bot token
- `GOOGLE_CLIENT_ID`: Google OAuth 2.0 client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth 2.0 client secret
- `CDK_DEFAULT_ACCOUNT`: Your AWS account ID
- `CDK_DEFAULT_REGION`: AWS region for deployment

## Project Structure

The project is organized into two Maven modules:

1. `tbot/` - Contains the bot application code
   - `config/` - Configuration classes
   - `handler/` - Lambda handler
   - `service/` - Business logic
   - `model/` - Data models

2. `infra/` - Contains AWS CDK infrastructure code
   - AWS Lambda function configuration
   - DynamoDB table setup
   - API Gateway configuration
   - EventBridge rules

## Building the Project

1. Build all modules:
```bash
mvn clean package
```

This will build both the bot application and infrastructure code.

## Deployment

1. Deploy the infrastructure:
```bash
cd infra
cdk deploy
```

2. After deployment, you'll get an API Gateway URL. Use this URL to set up your Telegram webhook:
```
https://api.telegram.org/bot<YOUR_BOT_TOKEN>/setWebhook?url=<API_GATEWAY_URL>
```

## Usage

1. Start the bot in Telegram by sending `/start`
2. Use `/help` to see available commands
3. Use `/words` to see today's vocabulary words
4. Use `/settings` to view your current settings

## Architecture

- The bot is deployed as an AWS Lambda function
- API Gateway handles incoming Telegram webhook requests
- EventBridge triggers the Lambda function on a schedule
- DynamoDB stores user vocabulary data
- Google Translate API is used to fetch favorites

## Development

The project uses a multi-module Maven structure:
- `tbot/` - Contains the core bot functionality
- `infra/` - Contains AWS CDK infrastructure code

## License

MIT License

<!-- Trigger build -->
