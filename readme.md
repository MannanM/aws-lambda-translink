# translink-lambda

## Translink notification and remediation

Do you hate being charged for not tapping on or off when travelling on Queensland Translink?

Do you hate not being told when you have been charged, and if you miss the flash up on the screen you might not even notice?

Do you have the painful process to lodge a dispute?

If any of those ring true for you this AWS Lambda is for you.
This will run on a schedule of your choosing and check your recent trips.
If there are any in the last 24 hours it will send you an email with the details so what journeys were mand and how much you were charged.

You can also take it a step further and get it to automatically lodge a dispute for you.
This works best when you only use your GoCard for a single purpose, e.g. work travel.

## How does it works?

| Variable | Required | Example | Description |
| -------- | -------- | ------- | ----------- |
| CARD_NUM | Yes      | 01600123451234567 | Your Go Card number |
| PASSWORD | Yes      | secret123 | Your Translink Go Card password |
| EMAIL    | No       | test@email.com | The email you want to receive summary reports at |
| MODE     | No*      | Train | Your regular mode of transport, i.e Bus, Ferry, Train or Tram |
| ROUTE    | No*      | Beenliegh Line | The normal route you take |
| SOURCE_DESTINATION  | No* | Yeronga:Central | Your regular start and end stop of your journey |
| DESCRIPTION | No*   | I was travelling too and from work as normal and was charged a penalty when tapping on. | The message you want to send |

\* If you want it to auto-lodge disputes, then all * attributes have to be supplied

## Instructions

- Update `build.gradle` variables to your desired values.
- Run `./gradlew createRole createLambda` - This will create an IAM role and upload the new Lambda using the role
- To make the lambda run at a schedule (e.g. 10pm every night) you can do so in the AWS console or via the CLI below.

```bash
aws events put-rule --name everyday-at-10pm \
    --schedule-expression 'cron(0 12 ? * MON-FRI *)'
    --description 'Executes everyday at 10pm'
aws lambda add-permission \
    --function-name translink-lambda \
    --statement-id my-scheduled-event \
    --action 'lambda:InvokeFunction' \
    --principal events.amazonaws.com \
    --source-arn arn:aws:events:us-east-1:123456789012:rule/everyday-at-10pm
echo '[{"Id": "1", "Arn": "arn:aws:lambda:us-east-1:123456789012:function:translink-lambda"}]' > targets.json
aws events put-targets --rule everyday-at-10pm --targets file://targets.json
```
Please replace the region and AWS account ID accordingly.

- If you want to update any Kotlin code changes simply run `./gradlew updateLambda` to push the latest code.

# Improvements

This was just me experimenting with Kotlin lambdas and trying to solve a specific frustration I had. 
There are a lot of things that could be improved such as:

- Better trip dispute logic.
- AWS Secret Manager integration.
- AWS Cloudformation template.
- More tests.
- Nicer email format.
