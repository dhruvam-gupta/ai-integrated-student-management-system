package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.StudentRepository;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    private final StudentRepository studentRepository;
    private final StreamingChatModel streamingChatModel;
    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    //todo: this will keep all the student objects in vector store. This could take up a lot of memory if there are many students
    //check on this further about which approach is better suggested here
    public Student createStudent(Student student) {
         Student newStudent = studentRepository.save(student);
         try {
            vectorStore.add(List.of(toDocument(newStudent)));
         } catch (Exception e) {
            log.error("VectorStore add failed for student {}", newStudent.getId(), e);
         }
         return newStudent;
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
            Student updatedStudent =  studentRepository.save(student);
            try {
                vectorStore.delete(List.of(id.toString()));
                vectorStore.add(List.of(toDocument(updatedStudent)));
            } catch (Exception e) {
                log.error("VectorStore update failed for student {}", id, e);
            }
            return updatedStudent;
        }).orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
        try{
            vectorStore.delete(List.of(id.toString()));
        } catch (Exception e) {
            log.error("VectorStore delete failed for student {}", id, e);
        }
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

    public Flux<String> generateStreamingStudentReport(Long id) {
        Student student = getStudentById(id);
        String prompt = "Generate a detailed academic report for student: " + student.getFirstName() + " " + student.getLastName() + 
                       " with email: " + student.getEmail() + " and major: " + student.getMajor();
        return streamingChatModel.stream(new Prompt(prompt))
        .mapNotNull(response -> response.getResult().getOutput().getText());
    }

    public String chat(String conversationId, String userMessage) {
        return chatClient.prompt()
            .user(userMessage)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();
    }

    private Document toDocument(Student s) {
        return new Document(
            s.getId().toString(),
            "Student " + s.getFirstName() + " " + s.getLastName() +
            " studies " + s.getMajor(),
            Map.of("studentId", s.getId()) // metadata
        );
    }
}