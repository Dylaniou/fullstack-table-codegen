package com.codegenerator.backend.model;

import javax.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Schema(name = "User", description = "User entity representing user data")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "Unique identifier", example = "1", readOnly = true)
    private Integer id;

    @Column(name = "mobile")
    @Schema(description = "Mobile")
    private String mobile;

    @Column(name = "passwd")
    @Schema(description = "Passwd")
    private String passwd;

    @Column(name = "name")
    @Schema(description = "Name")
    private String name;

    @Column(name = "sex")
    @Schema(description = "Sex")
    private String sex;

    @Column(name = "age")
    @Schema(description = "Age")
    private String age;

    @Column(name = "birthday")
    @Schema(description = "Birthday")
    private LocalDateTime birthday;

    @Column(name = "area")
    @Schema(description = "Area")
    private String area;

    @Column(name = "score")
    @Schema(description = "Score")
    private Double score;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    // toString method
    @Override
    public String toString() {
        return "User[id=" + id + "," +
        "mobile=" + mobile + "," +
        "passwd=" + passwd + "," +
        "name=" + name + "," +
        "sex=" + sex + "," +
        "age=" + age + "," +
        "birthday=" + birthday + "," +
        "area=" + area + "," +
        "score=" + score + "," +
        "]";
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}