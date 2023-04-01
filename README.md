# Stock-Exchange-System

Collaborator
* Yu Xiong
* Wenxi Zhong - wz173

# 2. Tech Stacks
Java + DOM Parser + PostgreSQL


# 3. Design & Implementation

## 2.1 MultiThread Implementation
*Strategy*: In the server side, we use `threadPool` to do concurrency control, otherwise, too many requests will cause us Out Of Memory

I used Netty framework to establish connection among server and clients. Netty provide built-in methods for realizing threadpools and Socket programming. The workflows of how Server handles a connection and assign it to a WorkerGroup is as follows:

![image](https://user-images.githubusercontent.com/101923398/229297448-a59833df-8a37-4160-b260-5099fb20c95f.png)



## 2.2 Socket Connection Implementation
Establishing Connection using Java Socket methods. 

## 2.3 XML Parsing
One option is to use Java DOM. Link is as follows:     
https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm


## 2.4 Database Interaction
&nbsp;  a. Object Relational Mapping(ORM) options: 1. Hibernates, 2.myBatis     
&nbsp;  b. Directly interact with database using SQL     

Reference:    
https://www.javatpoint.com/hibernate-transaction-management-example
