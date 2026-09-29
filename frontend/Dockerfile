# 多阶段构建：node 编译出静态资源，nginx 托管并反向代理 /api 到后端
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
# 淘宝 npm 镜像，国内安装依赖更快
RUN npm install --registry=https://registry.npmmirror.com
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
