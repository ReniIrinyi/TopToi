FROM node:18

WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar

COPY package*.json ./

RUN npm install

COPY . .


EXPOSE 3000

CMD ["npm", "run", "dev"]
