package com.mycompany.mavenproject4;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import graphql.ExecutionResult;
import graphql.GraphQL;
import static spark.Spark.port;
import static spark.Spark.post;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        GraphQL graphql = GraphQLConfig.init();
        Gson gson = new Gson();

        port(4567);
        post("/graphql", (req, res) -> {
            res.type("application/json");

            JsonObject request = gson.fromJson(req.body(), JsonObject.class);
            String query = request.get("query").getAsString();

            ExecutionResult result = graphql.execute(query);
            return gson.toJson(result.toSpecification());
        });
    }
}