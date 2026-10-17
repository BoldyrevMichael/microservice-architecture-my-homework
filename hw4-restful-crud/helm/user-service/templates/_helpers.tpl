{{/* общие метки и блок переменных окружения для подключения к БД */}}
{{- define "user-service.labels" -}}
app: {{ .Release.Name }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{- define "user-service.dbEnv" -}}
- name: DB_URL
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: DB_URL
- name: DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.db.secretName }}
      key: username
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.db.secretName }}
      key: password
{{- end }}
