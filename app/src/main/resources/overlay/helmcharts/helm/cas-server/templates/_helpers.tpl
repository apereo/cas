{{/*
Expand the name of the chart.
*/}}
{{- define "cas-server.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "cas-server.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create a name for boot admin deployment
*/}}
{{- define "cas-server.bootadminname" -}}
{{- $bootadminsuffix := default "boot-admin" .Values.bootadminSuffixOverride }}
{{- printf "%s-%s" (include "cas-server.fullname" . | trunc 43 | trimSuffix "-") $bootadminsuffix }}
{{- end }}

{{/*
Create a name for cas mgmt deployment
*/}}
{{- define "cas-server.mgmtname" -}}
{{- $mgmtsuffix := default "mgmt" .Values.mgmtSuffixOverride }}
{{- printf "%s-%s" (include "cas-server.fullname" . | trunc 43 | trimSuffix "-") $mgmtsuffix }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "cas-server.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "cas-server.labels" -}}
helm.sh/chart: {{ include "cas-server.chart" . }}
{{ include "cas-server.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "cas-server.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cas-server.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Bootadmin Selector labels
*/}}
{{- define "cas-bootadmin.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cas-server.bootadminname" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Bootadmin Pod labels
*/}}
{{- define "cas-bootadmin.labels" -}}
cas.server-type: bootadmin
{{- end }}

{{/*
CAS Mgmt Selector labels
*/}}
{{- define "cas-mgmt.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cas-server.mgmtname" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
CAS Mgmt Pod labels
*/}}
{{- define "cas-mgmt.labels" -}}
cas.server-type: mgmt
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "cas-server.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "cas-server.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Return the proper cas-server image name
*/}}
{{- define "cas-server.imageName" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.image "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper cas-server boot admin image name
*/}}
{{- define "cas-server.bootadminImageName" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.bootadminimage "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper CAS management image name
*/}}
{{- define "cas-server.mgmtImageName" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.mgmtimage "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper image name (for the init container volume-permissions image)
*/}}
{{- define "cas-server.volumePermissions.image" -}}
{{ include "common.images.image" (dict "imageRoot" .Values.volumePermissions.image "global" .Values.global) }}
{{- end -}}

{{/*
Return the proper image name
{{ include "common.images.image" ( dict "imageRoot" .Values.path.to.the.image "global" $) }}
*/}}
{{- define "common.images.image" -}}
{{- $registryName := .imageRoot.registry -}}
{{- $repositoryName := .imageRoot.repository -}}
{{- $tag := default "latest" .imageRoot.tag  | toString -}}
{{- if .global }}
    {{- if .global.imageRegistry }}
     {{- $registryName = .global.imageRegistry -}}
    {{- end -}}
{{- end -}}
{{- if ne $registryName "" }}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s:%s" $repositoryName $tag -}}
{{- end -}}
{{- end -}}


{{/*
Return log directory volume
*/}}
{{- define "cas-server.logdir" -}}
{{- if .Values.logdir.hostPath -}}
hostPath:
  path: {{ .Values.logdir.hostPath }}
  type: Directory
{{- else if .Values.logdir.claimName -}}
persistentVolumeClaim:
  claimName: {{ .Values.logdir.claimName }}
{{- else -}}
emptyDir: {}
{{- end }}
{{- end -}}


{{/*
Renders a value that contains template.
Usage:
{{ include "cas-server.tplvalues.render" ( dict "value" .Values.path.to.the.Value "context" $) }}
*/}}
{{- define "cas-server.tplvalues.render" -}}
    {{- if typeIs "string" .value }}
        {{- tpl .value .context }}
    {{- else }}
        {{- tpl (.value | toYaml) .context }}
    {{- end }}
{{- end -}}
