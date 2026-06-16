# Stage 1: Build Angular app
FROM node:22 AS build
WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .
RUN npx ng build --configuration production

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy Angular build output into Nginx's default html directory
COPY --from=build /app/dist/M2SWebFE /usr/share/nginx/html

# Copy your custom Nginx config into the container
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
