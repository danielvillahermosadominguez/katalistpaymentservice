package com.codurance.katalyst.payment.application.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface DBCourseRepository extends CrudRepository<DBCourse, Long> {
    Optional<DBCourse> findByCourseId(long courseId);
}
