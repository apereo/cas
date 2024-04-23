#!/bin/bash

echo "Running Syncope Docker image..."
COMPOSE_FILE=./ci/tests/syncope/docker-compose.yml
test -f $COMPOSE_FILE || COMPOSE_FILE=docker-compose.yml
docker-compose -f $COMPOSE_FILE down >/dev/null 2>/dev/null || true
docker-compose -f $COMPOSE_FILE up -d
docker logs syncope-syncope-1 -f &
echo -e "Waiting for Syncope server to come online...\n"
sleep 60
until $(curl --output /dev/null --silent --head --fail http://localhost:18080/syncope/); do
    printf '.'
    sleep 1
done
echo -e "\nSyncope docker container is running."

echo "Creating CSV Dir conn instance"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/connectors' \
  -H 'accept: */*' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "key": "535557bd-8785-4bd3-9557-bd87858bd3e7",
  "errored": false,
  "adminRealm": "/",
  "location": "file:/opt/syncope/bundles",
  "connectorName": "net.tirasa.connid.bundles.csvdir.CSVDirConnector",
  "bundleName": "net.tirasa.connid.bundles.csvdir",
  "version": "0.8.9",
  "displayName": "CSV",
  "connRequestTimeout": 10,
  "poolConf": null,
  "conf": [
    {
      "schema": {
        "name": "sourcePath",
        "displayName": "Source path",
        "helpMessage": "Absolute path of a directory where the CSV files to be processed are located",
        "type": "java.lang.String",
        "required": true,
        "order": 1,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": true,
      "values": [
        "/tmp"
      ]
    },
    {
      "schema": {
        "name": "fileMask",
        "displayName": "File mask",
        "helpMessage": "Regular expression describing files to be processed",
        "type": "java.lang.String",
        "required": true,
        "order": 2,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": [
        "default.csv"
      ]
    },
    {
      "schema": {
        "name": "encoding",
        "displayName": "File encoding",
        "helpMessage": "Basic encoding of the file",
        "type": "java.lang.String",
        "required": false,
        "order": 3,
        "confidential": false,
        "defaultValues": [
          "UTF-8"
        ]
      },
      "overridable": false,
      "values": [
        "UTF-8"
      ]
    },
    {
      "schema": {
        "name": "fieldDelimiter",
        "displayName": "Field delimiter",
        "helpMessage": "Delimiter used to separate fields in CSV files. Default is \",\".",
        "type": "char",
        "required": false,
        "order": 4,
        "confidential": false,
        "defaultValues": [
          ","
        ]
      },
      "overridable": false,
      "values": [
        ","
      ]
    },
    {
      "schema": {
        "name": "textQualifier",
        "displayName": "Text qualifier",
        "helpMessage": "Delimiter to determine beginning and end of text in value. Default is \".",
        "type": "char",
        "required": false,
        "order": 5,
        "confidential": false,
        "defaultValues": [
          "\""
        ]
      },
      "overridable": false,
      "values": [
        "\""
      ]
    },
    {
      "schema": {
        "name": "keyColumnNames",
        "displayName": "Key column name",
        "helpMessage": "Name of the column used to identify user uniquely",
        "type": "[Ljava.lang.String;",
        "required": true,
        "order": 6,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": [
        "key"
      ]
    },
    {
      "schema": {
        "name": "passwordColumnName",
        "displayName": "Password column name",
        "helpMessage": "Name of the column used to specify user password",
        "type": "java.lang.String",
        "required": false,
        "order": 7,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": []
    },
    {
      "schema": {
        "name": "deleteColumnName",
        "displayName": "Delete column name",
        "helpMessage": "Name of the column used to specify users to be deleted",
        "type": "java.lang.String",
        "required": false,
        "order": 8,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": []
    },
    {
      "schema": {
        "name": "quotationRequired",
        "displayName": "Value quotation required",
        "helpMessage": "Specify if value quotation is required. Default is true.",
        "type": "java.lang.Boolean",
        "required": false,
        "order": 9,
        "confidential": false,
        "defaultValues": [
          true
        ]
      },
      "overridable": false,
      "values": [
        "false"
      ]
    },
    {
      "schema": {
        "name": "fields",
        "displayName": "Column names",
        "helpMessage": "Column names",
        "type": "[Ljava.lang.String;",
        "required": true,
        "order": 10,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": [
        "key",
        "virtual"
      ]
    },
    {
      "schema": {
        "name": "ignoreHeader",
        "displayName": "Ignore header",
        "helpMessage": "Specify if first line of the file must be ignored. Default is true.",
        "type": "java.lang.Boolean",
        "required": false,
        "order": 11,
        "confidential": false,
        "defaultValues": [
          true
        ]
      },
      "overridable": false,
      "values": [
        "false"
      ]
    },
    {
      "schema": {
        "name": "keyseparator",
        "displayName": "Key separator",
        "helpMessage": "Character used to separate keys in a multi-key scenario. Default is \",\".",
        "type": "java.lang.String",
        "required": false,
        "order": 12,
        "confidential": false,
        "defaultValues": [
          ","
        ]
      },
      "overridable": false,
      "values": [
        ","
      ]
    },
    {
      "schema": {
        "name": "multivalueSeparator",
        "displayName": "Multi value separator",
        "helpMessage": "Character used to separate values in a multi-value scenario. Multivalue unsupported if not provided.",
        "type": "java.lang.String",
        "required": false,
        "order": 13,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": []
    },
    {
      "schema": {
        "name": "defaultStatusValue",
        "displayName": "Default Status Value",
        "helpMessage": "Enter the value for status in case of status not specified. Default is \"true\".",
        "type": "java.lang.String",
        "required": false,
        "order": 14,
        "confidential": false,
        "defaultValues": [
          "true"
        ]
      },
      "overridable": false,
      "values": [
        "true"
      ]
    },
    {
      "schema": {
        "name": "disabledStatusValue",
        "displayName": "Disabled Status Value",
        "helpMessage": "Specify a value for disabled status. Default is \"false\".",
        "type": "java.lang.String",
        "required": false,
        "order": 15,
        "confidential": false,
        "defaultValues": [
          "false"
        ]
      },
      "overridable": false,
      "values": [
        "false"
      ]
    },
    {
      "schema": {
        "name": "enabledStatusValue",
        "displayName": "Enable Status Value",
        "helpMessage": "Specify a value for enabled status. Default is \"true\".",
        "type": "java.lang.String",
        "required": false,
        "order": 16,
        "confidential": false,
        "defaultValues": [
          "true"
        ]
      },
      "overridable": false,
      "values": [
        "true"
      ]
    },
    {
      "schema": {
        "name": "statusColumn",
        "displayName": "Status Column name",
        "helpMessage": "Status column name.",
        "type": "java.lang.String",
        "required": false,
        "order": 17,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": []
    },
    {
      "schema": {
        "name": "objectClassColumn",
        "displayName": "ObjectClass Column Name",
        "helpMessage": "Column name identifying identity record type",
        "type": "java.lang.String",
        "required": false,
        "order": 18,
        "confidential": false,
        "defaultValues": []
      },
      "overridable": false,
      "values": []
    },
    {
      "schema": {
        "name": "objectClass",
        "displayName": "Supported Object Classes",
        "helpMessage": "Supported object classes (__ACCOUNT__ as default if empty)",
        "type": "[Ljava.lang.String;",
        "required": false,
        "order": 19,
        "confidential": false,
        "defaultValues": [
          "__ACCOUNT__"
        ]
      },
      "overridable": false,
      "values": []
    }
  ],
  "capabilities": [
    "SEARCH", "CREATE", "UPDATE", "SYNC"
  ]
}' | jq

echo "-----------------"


key=`curl -X 'GET' \
  'http://localhost:18080/syncope/rest/connectors' \
  -H 'accept: application/json' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' | jq .[].key | sed 's/"//g'`

echo "Creating resource from connector $key"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/resources' \
  -H 'accept: */*' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "key": "import",
  "connector": "'$key'",
  "connectorDisplayName": "CSV",
  "orgUnit": null,
  "propagationPriority": null,
  "randomPwdIfNotProvided": false,
  "enforceMandatoryCondition": false,
  "createTraceLevel": "ALL",
  "updateTraceLevel": "ALL",
  "deleteTraceLevel": "ALL",
  "provisioningTraceLevel": "ALL",
  "passwordPolicy": null,
  "accountPolicy": null,
  "propagationPolicy": null,
  "pullPolicy": null,
  "pushPolicy": null,
  "provisionSorter": null,
  "overrideCapabilities": false,
  "provisions": [
    {
      "anyType": "USER",
      "objectClass": "__ACCOUNT__",
      "syncToken": null,
      "ignoreCaseMatch": false,
      "uidOnCreate": null,
      "mapping": {
        "connObjectLink": null,
        "connObjectKeyItem": {
          "key": "4b40f246-362d-4caf-80f2-46362dacaf14",
          "intAttrName": "username",
          "extAttrName": "key",
          "connObjectKey": true,
          "password": false,
          "mandatoryCondition": "true",
          "purpose": "BOTH",
          "propagationJEXLTransformer": null,
          "pullJEXLTransformer": null,
          "transformers": []
        },
        "items": [
          {
            "intAttrName": "username",
            "extAttrName": "key",
            "connObjectKey": true,
            "password": false,
            "mandatoryCondition": "true",
            "purpose": "BOTH",
            "propagationJEXLTransformer": null,
            "pullJEXLTransformer": null,
            "transformers": []
          }
        ],
        "linkingItems": []
      },
      "auxClasses": [],
      "virSchemas": []
    }
  ],
  "confOverride": [],
  "capabilitiesOverride": [],
  "propagationActions": []
}' | jq

echo "-----------------"

echo "Creating virtual schema"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/schemas/VIRTUAL' \
  -H 'accept: */*' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '  {
    "_class": "org.apache.syncope.common.lib.to.VirSchemaTO",
    "key": "toBeVirtualized",
    "labels": {},
    "readonly": false,
    "resource": "import",
    "anyType": "USER",
    "extAttrName": "virtual"
}' | jq

echo "Assigning virtual schema to BaseUser"
curl -X 'PUT' \
  'http://localhost:18080/syncope/rest/anyTypeClasses/BaseUser' \
  -H 'accept: */*' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "key": "BaseUser",
  "plainSchemas": [
    "email"
  ],
  "derSchemas": [
    "description"
  ],
  "virSchemas": [
    "toBeVirtualized"
  ],
  "inUseByTypes": [
    "USER"
  ]
}' | jq

echo "-----------------"

echo "Creating derived attribute"
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/schemas/DERIVED' \
  -H 'accept: */*' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
    "_class": "org.apache.syncope.common.lib.to.DerSchemaTO",
    "key": "description",
    "anyTypeClass": "BaseUser",
    "labels": {},
    "expression": "'\''email: '\'' + email"
  }
' | jq

echo -e "\n-----------------\n"

echo "Creating sample user: syncopecas..."
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/users?storePassword=true' \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
        "_class": "org.apache.syncope.common.lib.request.UserCR",
        "realm": "/",
        "username": "syncopecas",
        "password": "Mellon",
        "plainAttrs": [
            {
              "schema": "email",
              "values": [
                "syncopecas@syncope.org"
              ]
            }
        ],
        "derAttrs": [
          {
            "schema": "description"
          }
        ],
        "virAttrs": [
          {
            "schema": "toBeVirtualized"
          }
        ]
  }'

echo -e "\n-----------------\n"

echo "Creating sample user: casuser..."
curl -X 'POST' \
  'http://localhost:18080/syncope/rest/users?storePassword=true' \
  -H 'accept: application/json' \
  -H 'Prefer: return-content' \
  -H 'X-Syncope-Null-Priority-Async: false' \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/json' \
  -d '{
  "_class": "org.apache.syncope.common.lib.request.UserCR",
  "realm": "/",
  "username": "casuser",
  "password": "Sync0pe",
  "plainAttrs": [
    {
      "schema": "email",
      "values": [
        "casuser@syncope.org"
      ]
    }
  ],
  "derAttrs": [
    {
      "schema": "description"
    }
  ],
  "virAttrs": [
    {
      "schema": "toBeVirtualized"
    }
  ]
}'

echo -e "\nReady!\n"
