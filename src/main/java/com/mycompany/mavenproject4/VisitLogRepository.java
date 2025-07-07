package com.mycompany.mavenproject4;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VisitLogRepository {
    private static List<VisitLog> logs = new ArrayList<>();
    private static int idCounter = 1;

    public static VisitLog add(String studentName, String studentId, String studyProgram, String purpose) {
        VisitLog log = new VisitLog(idCounter++, studentName, studentId, studyProgram, purpose, LocalDateTime.now());
        logs.add(log);
        return log;
    }

    public static List<VisitLog> findAll() {
        return logs;
    }

    public static VisitLog findById(int id) {
        return logs.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public static VisitLog update(int id, String studentName, String studentId, String studyProgram, String purpose) {
        VisitLog log = findById(id);
        if (log != null) {
            log.setStudentName(studentName);
            log.setStudentId(studentId);
            log.setStudyProgram(studyProgram);
            log.setPurpose(purpose);
        }
        return log;
    }

    public static boolean delete(int id) {
        return logs.removeIf(p -> p.getId() == id);
    }
}
