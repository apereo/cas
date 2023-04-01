ARG GCLOUD_SDK_VERSION=424.0.0-alpine

FROM gcr.io/google.com/cloudsdktool/google-cloud-cli:$GCLOUD_SDK_VERSION

# Install Java 8 JRE (required for Firestore emulator).
RUN apk add --update --no-cache openjdk11

# Install Emulators
RUN gcloud components install cloud-firestore-emulator beta
RUN gcloud components install pubsub-emulator beta
RUN gcloud components update

COPY entrypoint.sh .

ENV FIRESTORE_PORT 9980
EXPOSE "$FIRESTORE_PORT"

ENV PUBSUB_PORT 8085
EXPOSE "$PUBSUB_PORT"

ENV GCP_PROJECT_ID "apereo-cas-gcp"

ENTRYPOINT ["./entrypoint.sh"]
