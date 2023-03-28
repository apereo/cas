ARG GCLOUD_SDK_VERSION=423.0.0-alpine

FROM gcr.io/google.com/cloudsdktool/google-cloud-cli:$GCLOUD_SDK_VERSION

# Install Java 8 JRE (required for Firestore emulator).
RUN apk add --update --no-cache openjdk11

# Install Firestore Emulator.
RUN gcloud components install cloud-firestore-emulator beta --quiet

COPY entrypoint.sh .

ENV PORT 8080
EXPOSE "$PORT"

ENV FIRESTORE_PROJECT_ID "apereo-cas-firestore"

ENTRYPOINT ["./entrypoint.sh"]
