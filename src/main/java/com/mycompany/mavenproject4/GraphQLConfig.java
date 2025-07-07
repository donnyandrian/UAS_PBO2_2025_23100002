package com.mycompany.mavenproject4;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLConfig {
    public static GraphQL init() throws IOException {
        InputStream schemaStream = GraphQLConfig.class.getClassLoader().getResourceAsStream("schema.graphqls");

        if (schemaStream == null) {
            throw new RuntimeException("schema.graphqls not found in classpath.");
        }

        String schema = new String(Objects.requireNonNull(schemaStream).readAllBytes());

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schema);
        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("allVisitLogs", env -> {
                            var logs = VisitLogRepository.findAll();

                            List<Object> res = new ArrayList<>();
                            for (int i = 0; i < logs.size(); i++) {
                                res.add(visitLogToObject(logs.get(i)));
                            }

                            return res;
                        })
                        .dataFetcher("logById", env -> {
                            int id = env.getArgument("id");
                            return visitLogToObject(VisitLogRepository.findById(id));
                        }))
                .type("Mutation", builder -> builder
                        .dataFetcher("addVisitLog", env -> visitLogToObject(VisitLogRepository.add(
                            env.getArgument("studentName"),
                            env.getArgument("studentId"),
                            env.getArgument("studyProgram"),
                            env.getArgument("purpose"))))
                        .dataFetcher("updateVisitLog", env -> {
                            var updated = VisitLogRepository.update(env.getArgument("id"),
                                env.getArgument("studentName"),
                                env.getArgument("studentId"),
                                env.getArgument("studyProgram"),
                                env.getArgument("purpose"));

                            return visitLogToObject(updated);
                        })
                        .dataFetcher("deleteVisitLog", env -> {
                            int id = Integer.parseInt(env.getArgument("id"));
                            return VisitLogRepository.delete(id);
                        }))
                .build();

        GraphQLSchema schemaFinal = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        return GraphQL.newGraphQL(schemaFinal).build();
    }

    private static Object visitLogToObject(VisitLog log) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = log.getVisitTime().format(dateTimeFormatter);
                                
        var obj = new VisitLog2(log.getId(), log.getStudentName(),
                log.getStudentId(), log.getStudyProgram(), log.getPurpose(), formattedDateTime);

        var ssl = new Gson().toJson(obj);
        System.out.println(ssl);
        return obj;
    }
}
