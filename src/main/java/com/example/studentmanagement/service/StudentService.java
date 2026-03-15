package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.StudentRepository;
import org.springframework.ai.chat.model.ChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final ChatModel chatModel;

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Student updateStudent(Long id, Student studentDetails) {
        return studentRepository.findById(id).map(student -> {
            student.setFirstName(studentDetails.getFirstName());
            student.setLastName(studentDetails.getLastName());
            student.setEmail(studentDetails.getEmail());
            student.setPhone(studentDetails.getPhone());
            student.setDateOfBirth(studentDetails.getDateOfBirth());
            student.setMajor(studentDetails.getMajor());
            student.setGpa(studentDetails.getGpa());
            student.setActive(studentDetails.isActive());
            return studentRepository.save(student);
        }).orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public List<Student> getStudentsByMajor(String major) {
        return studentRepository.findByMajor(major);
    }

    public List<Student> getStudentsByIsActive(boolean isActive) {
        return studentRepository.findByIsActive(isActive);
    }

    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public long getTotalStudents() {
        return studentRepository.count();
    }
    
    public String generateStudentReport(Long id) {
        Student student = getStudentById(id);
        String prompt = "Generate a detailed academic report for student: " + student.getFirstName() + " " + student.getLastName() + 
                       " with email: " + student.getEmail() + " and major: " + student.getMajor();
        return chatModel.call(prompt);
    }
}