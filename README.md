# Stock-Exchange-System

Collaborator
* Yu Xiong - yx236
* Wenxi Zhong - wz173

# 1. How to run
To run this project, do sudo docker-compose up, and the server would start running in docker.

# 2. Testing
We put unit tests in src/test/java.
We provided some test xml cases under testing folder, which include correct syntax. 


# 3. Design & Implementation

## 3.1 Tech Stacks
Netty + Java DOM Parser + Hibernates + PostgreSQL

## 3.2 MultiThread Implementation
We used Netty framework to establish connection among server and clients. Netty provide built-in methods for realizing Multithread programming and Socket programming. The workflows of how Server handles a connection and assign it to a WorkerGroup is as follows:

![image](https://user-images.githubusercontent.com/101923398/229297448-a59833df-8a37-4160-b260-5099fb20c95f.png)


## 3.3 Socket Connection Implementation
Establishing Connection using Netty.

## 3.4 XML Parsing
One option is to use Java DOM. Link is as follows:     
https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm


## 3.4 Database Interaction  
&nbsp;  Directly interact with database using SQL     

Reference:    
https://www.javatpoint.com/hibernate-transaction-management-example
