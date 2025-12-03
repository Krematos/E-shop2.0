# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Copy custom nginx config if we have one, assuming it's passed in build context or available
# Since this Dockerfile is in deploy/, and nginx conf is in deploy/nginx/default.conf
# And we plan to run build with context ../frontend, we can't easily access deploy/nginx/default.conf from inside the build context of frontend ONLY if we don't include it.
# BUT, we can mount it in docker-compose, which is often cleaner.
# HOWEVER, to make the image self-contained:
# We might need to adjust the build context or copy strategy.
# Let's assume for now we will mount it in docker-compose or copy it if context allows.
# Actually, let's just use the default nginx config for now and rely on docker-compose volume mount for the custom config, 
# OR better: COPY it in the Dockerfile if we can. 
# If context is `frontend/`, we can't access `../deploy/nginx`.
# So we will rely on docker-compose to mount the config, OR we change context.
# Let's stick to standard: Image contains code, Config can be injected.
# BUT for a "deployable" image, it's nice to have config inside.
# Let's rely on docker-compose mounting for the config for now, as it's flexible.
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
