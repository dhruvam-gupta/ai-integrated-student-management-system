package com.example.studentmanagement.controller;

import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Student>> getAllStudents(Pageable pageable) {
        Page<Student> students = studentService.getAllStudents(pageable);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        try {
            Student updatedStudent = studentService.updateStudent(id, studentDetails);
            return new ResponseEntity<>(updatedStudent, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/major/{major}")
    public ResponseEntity<List<Student>> getStudentsByMajor(@PathVariable String major) {
        List<Student> students = studentService.getStudentsByMajor(major);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping("/isActive/{isActive}")
    public ResponseEntity<List<Student>> getStudentsByIsActive(@PathVariable boolean isActive) {
        List<Student> students = studentService.getStudentsByIsActive(isActive);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping("/search/email")
    public ResponseEntity<Student> getStudentByEmail(@RequestParam String email) {
        Optional<Student> student = studentService.getStudentByEmail(email);
        return student.map(s -> new ResponseEntity<>(s, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalStudents() {
        long count = studentService.getTotalStudents();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<?> generateReport(@PathVariable Long id) {
        String report = studentService.generateStudentReport(id);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping(value = "/streaming/report/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<?> generateStreamingReport(@PathVariable Long id) {
        Flux<String> report = studentService.generateStreamingStudentReport(id);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}