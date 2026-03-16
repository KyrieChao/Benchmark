package com.chao.benchmark;

import com.chao.failfast.Failure;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.*;

/**
 * 复杂对象验证性能测试类
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
public class ComplexObjectValidationBenchmark {

    // 测试数据
    private static final String validUsername = "testuser";
    private static final String invalidUsername = "";
    private static final int validAge = 20;
    private static final int invalidAge = 15;

    // 响应码
    private static final ResponseCode USERNAME_REQUIRED = ResponseCode.of(40001, "Username is required");
    private static final ResponseCode AGE_TOO_YOUNG = ResponseCode.of(40003, "Age is too young");
    private static final ResponseCode LIST_NOT_EMPTY = ResponseCode.of(40004, "List cannot be empty");
    private static final ResponseCode SET_NOT_EMPTY = ResponseCode.of(40005, "Set cannot be empty");
    private static final ResponseCode MAP_NOT_EMPTY = ResponseCode.of(40006, "Map cannot be empty");

    // 测试次数
    private static final int TEST_COUNT = 1000000;

    // 嵌套对象
    private static class Address {
        private String street;
        private String city;
        private String zipCode;

        public Address(String street, String city, String zipCode) {
            this.street = street;
            this.city = city;
            this.zipCode = zipCode;
        }

        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getZipCode() { return zipCode; }
    }

    // 复杂对象
    private static class User {
        private String username;
        private int age;
        private Address address;
        private List<String> hobbies;
        private Set<String> roles;
        private Map<String, String> attributes;

        public User(String username, int age, Address address, List<String> hobbies, Set<String> roles, Map<String, String> attributes) {
            this.username = username;
            this.age = age;
            this.address = address;
            this.hobbies = hobbies;
            this.roles = roles;
            this.attributes = attributes;
        }

        public String getUsername() { return username; }
        public int getAge() { return age; }
        public Address getAddress() { return address; }
        public List<String> getHobbies() { return hobbies; }
        public Set<String> getRoles() { return roles; }
        public Map<String, String> getAttributes() { return attributes; }
    }

    public static void main(String[] args) {
        System.out.println("开始复杂对象验证性能测试...");
        System.out.println("测试次数: " + TEST_COUNT);
        System.out.println("=======================================");

        // 准备测试数据
        Address validAddress = new Address("123 Main St", "City", "12345");
        Address invalidAddress = new Address("", "City", "12345");

        List<String> validHobbies = new ArrayList<>();
        validHobbies.add("Reading");
        validHobbies.add("Sports");

        List<String> invalidHobbies = new ArrayList<>();

        Set<String> validRoles = new HashSet<>();
        validRoles.add("USER");
        validRoles.add("ADMIN");

        Set<String> invalidRoles = new HashSet<>();

        Map<String, String> validAttributes = new HashMap<>();
        validAttributes.put("key1", "value1");
        validAttributes.put("key2", "value2");

        Map<String, String> invalidAttributes = new HashMap<>();

        User validUser = new User(validUsername, validAge, validAddress, validHobbies, validRoles, validAttributes);
        User invalidUser = new User(invalidUsername, invalidAge, invalidAddress, invalidHobbies, invalidRoles, invalidAttributes);

        // 测试 1: 嵌套对象验证（有效数据）
        testNestedObjectValid(validUser);

        // 测试 2: 嵌套对象验证（无效数据）
        testNestedObjectInvalid(invalidUser);

        // 测试 3: 集合类型验证（有效数据）
        testCollectionValid(validHobbies, validRoles, validAttributes);

        // 测试 4: 集合类型验证（无效数据）
        testCollectionInvalid(invalidHobbies, invalidRoles, invalidAttributes);

        System.out.println("=======================================");
        System.out.println("复杂对象验证性能测试完成！");
    }

    // 测试 1: 嵌套对象验证（有效数据）
    private static void testNestedObjectValid(User user) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(user.getUsername(), USERNAME_REQUIRED)
                        .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                        .notBlank(user.getAddress().getStreet(), USERNAME_REQUIRED)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("嵌套对象验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 2: 嵌套对象验证（无效数据）
    private static void testNestedObjectInvalid(User user) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notBlank(user.getUsername(), USERNAME_REQUIRED)
                        .greaterOrEqual(user.getAge(), 18, AGE_TOO_YOUNG)
                        .notBlank(user.getAddress().getStreet(), USERNAME_REQUIRED)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("嵌套对象验证（无效数据）: " + avgTime + " ns/op");
    }

    // 测试 3: 集合类型验证（有效数据）
    private static void testCollectionValid(List<String> hobbies, Set<String> roles, Map<String, String> attributes) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notEmpty(hobbies, LIST_NOT_EMPTY)
                        .notEmpty(roles, SET_NOT_EMPTY)
                        .notEmpty(attributes, MAP_NOT_EMPTY)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("集合类型验证（有效数据）: " + avgTime + " ns/op");
    }

    // 测试 4: 集合类型验证（无效数据）
    private static void testCollectionInvalid(List<String> hobbies, Set<String> roles, Map<String, String> attributes) {
        long start = System.nanoTime();
        for (int i = 0; i < TEST_COUNT; i++) {
            try {
                Failure.begin()
                        .notEmpty(hobbies, LIST_NOT_EMPTY)
                        .notEmpty(roles, SET_NOT_EMPTY)
                        .notEmpty(attributes, MAP_NOT_EMPTY)
                        .fail();
            } catch (Exception e) {
                // 忽略异常
            }
        }
        long end = System.nanoTime();
        double avgTime = (end - start) / (double) TEST_COUNT;
        System.out.println("集合类型验证（无效数据）: " + avgTime + " ns/op");
    }
}
