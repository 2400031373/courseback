package com.example.backend;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api")
public class CourseController {

    private final List<Map<String, Object>> courses = new ArrayList<>();
    private final List<Map<String, Object>> submissions = new ArrayList<>();
    private final List<String> enrolledCourseIds = new ArrayList<>();

    public CourseController() {
        Map<String, Object> c1 = new HashMap<>();
        c1.put("id", "1");
        c1.put("title", "Java Programming");
        c1.put("category", "Programming");
        c1.put("level", "Beginner");
        c1.put("description", "Learn Java basics");
        c1.put("tags", Arrays.asList("java", "backend"));

        List<Map<String, Object>> modules = new ArrayList<>();
        List<Map<String, Object>> assignments = new ArrayList<>();

        Map<String, Object> a1 = new HashMap<>();
        a1.put("id", "a1");
        a1.put("title", "Assignment 1");
        a1.put("dueDate", "2026-04-10");
        a1.put("type", "Assignment");
        a1.put("maxMarks", 100);

        assignments.add(a1);

        c1.put("modules", modules);
        c1.put("assignments", assignments);

        courses.add(c1);
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> getCourses() {
        return courses;
    }

    @PostMapping("/courses")
    public Map<String, Object> createCourse(@RequestBody Map<String, Object> data) {
        Map<String, Object> course = new HashMap<>(data);
        course.put("id", UUID.randomUUID().toString());

        Object tags = course.get("tags");
        if (!(tags instanceof List<?>)) {
            course.put("tags", new ArrayList<>());
        }

        course.put("modules", new ArrayList<Map<String, Object>>());
        course.put("assignments", new ArrayList<Map<String, Object>>());

        courses.add(course);
        return course;
    }

    @PostMapping("/courses/{courseId}/modules")
    public Map<String, Object> addModule(
            @PathVariable String courseId,
            @RequestBody Map<String, Object> data
    ) {
        Map<String, Object> course = findCourseOrThrow(courseId);

        Map<String, Object> module = new HashMap<>(data);
        module.put("id", UUID.randomUUID().toString());

        List<Map<String, Object>> modules = getList(course, "modules");
        modules.add(module);

        return module;
    }

    @PostMapping("/courses/{courseId}/assignments")
    public Map<String, Object> addAssignment(
            @PathVariable String courseId,
            @RequestBody Map<String, Object> data
    ) {
        Map<String, Object> course = findCourseOrThrow(courseId);

        Map<String, Object> assignment = new HashMap<>(data);
        assignment.put("id", UUID.randomUUID().toString());

        Object maxMarks = assignment.get("maxMarks");
        if (maxMarks instanceof String) {
            try {
                assignment.put("maxMarks", Integer.parseInt((String) maxMarks));
            } catch (NumberFormatException ignored) {
                assignment.put("maxMarks", 100);
            }
        }

        List<Map<String, Object>> assignments = getList(course, "assignments");
        assignments.add(assignment);

        return assignment;
    }

    @PostMapping("/enroll")
    public String enroll(@RequestBody Map<String, String> data) {
        String courseId = data.get("courseId");
        if (courseId == null || courseId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseId is required");
        }

        if (!enrolledCourseIds.contains(courseId)) {
            enrolledCourseIds.add(courseId);
        }

        return "Enrolled";
    }

    @GetMapping("/enrollments")
    public List<String> getEnrollments() {
        return enrolledCourseIds;
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Map<String, Object> data) {
        Map<String, Object> submission = new HashMap<>(data);

        String courseId = String.valueOf(submission.get("courseId"));
        String assignmentId = String.valueOf(submission.get("assignmentId"));

        Map<String, Object> course = findCourseOrThrow(courseId);
        Map<String, Object> assignment = findAssignmentOrThrow(course, assignmentId);

        submission.put("id", UUID.randomUUID().toString());
        submission.put("status", "Submitted");
        submission.put("createdAt", new Date().toString());
        submission.put("courseTitle", course.get("title"));
        submission.put("assignmentTitle", assignment.get("title"));
        submission.put("assignmentType", assignment.get("type"));
        submission.put("dueDate", assignment.get("dueDate"));
        submission.put("maxMarks", assignment.getOrDefault("maxMarks", 100));
        submission.putIfAbsent("marks", null);
        submission.putIfAbsent("feedback", "");

        submissions.add(submission);
        return submission;
    }

    @GetMapping("/submissions")
    public List<Map<String, Object>> getSubmissions() {
        return submissions;
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public Map<String, Object> gradeSubmission(
            @PathVariable String submissionId,
            @RequestBody Map<String, Object> data
    ) {
        Map<String, Object> target = submissions.stream()
                .filter(s -> submissionId.equals(String.valueOf(s.get("id"))))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

        Object marks = data.get("marks");
        if (marks == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "marks is required");
        }

        target.put("marks", marks);
        target.put("feedback", data.getOrDefault("feedback", ""));
        target.put("status", "Graded");

        return target;
    }

    private Map<String, Object> findCourseOrThrow(String courseId) {
        return courses.stream()
                .filter(c -> courseId.equals(String.valueOf(c.get("id"))))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private Map<String, Object> findAssignmentOrThrow(Map<String, Object> course, String assignmentId) {
        List<Map<String, Object>> assignments = getList(course, "assignments");
        return assignments.stream()
                .filter(a -> assignmentId.equals(String.valueOf(a.get("id"))))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List<?>) {
            return (List<Map<String, Object>>) value;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        map.put(key, list);
        return list;
    }
}