{{/* имя сервиса = имя релиза */}}
{{- define "service.name" -}}
{{- .Release.Name -}}
{{- end -}}

{{- define "service.labels" -}}
app: {{ include "service.name" . }}
app.kubernetes.io/name: {{ include "service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{/* переменные окружения: простые значения из ConfigMap, пароли и ключи из Secret */}}
{{- define "service.env" -}}
{{- range $name, $_ := .Values.env }}
- name: {{ $name }}
  valueFrom:
    configMapKeyRef:
      name: {{ include "service.name" $ }}-config
      key: {{ $name }}
{{- end }}
{{- range $name, $ref := .Values.envFromSecret }}
- name: {{ $name }}
  valueFrom:
    secretKeyRef:
      name: {{ $ref.secret }}
      key: {{ $ref.key }}
{{- end }}
{{- end -}}

{{- define "service.initContainers" -}}
initContainers:
  - name: wait-for-db
    image: busybox:1.37
    command: ["sh", "-c", "until nc -z {{ .Values.waitFor.host }} {{ .Values.waitFor.port }}; do sleep 2; done"]
    resources:
      requests:
        cpu: 10m
        memory: 16Mi
      limits:
        cpu: 100m
        memory: 32Mi
{{- end -}}
