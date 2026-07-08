# build
FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# serve
FROM nginx:stable-alpine AS production-stage

# SPA 라우팅 대응용 nginx 설정 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Vite 빌드 결과물(dist) 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# SPA 라우팅 대응하려면 nginx.conf 커스텀 필요할 수 있음
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]