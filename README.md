# cdi-unit

[cdi-unit](https://cdi-unit.github.io/cdi-unit/)を検証するためのプロジェクトです。

## 関連リンク

* [cdi-unit](https://github.com/cdi-unit/cdi-unit/tree/master)

* [DatabaseRider](https://database-rider.github.io/database-rider/1.44.0/documentation.html?theme=foundation#_introduction)

* [weld-testing(Junit5)](https://github.com/weld/weld-testing/tree/master/junit5)

* [Transaction管理1](https://in.relation.to/2019/01/23/testing-cdi-beans-and-persistence-layer-under-java-se/)

* [Transaction管理2](https://stackoverflow.com/questions/59776325/weld-and-junit-no-transactionmanager)

* [deltaspike](https://deltaspike.apache.org/documentation/test-control.html)

* [Arquillian](https://docs.jboss.org/arquillian/reference/1.0.0.Alpha1/en-US/html_single/)

## 環境

<pre>
PS C:\Workspaces\jaguar> mvn -v
Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
Maven home: C:\Program Files\Maven\apache-maven-3.9.6
Java version: 17.0.9, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk-17
Default locale: ja_JP, platform encoding: MS932
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
</pre>

## 実行手順

1. mvn clean package -P wildfly-bootable-jar
1. java -jar  target/weld-testing-bootable.jar
1. http://127.0.0.1:8080/


## Arquillionの設定

echo $env:JBOSS_HOME 
$env:JBOSS_HOME="C:\Users\3kzey\OneDrive\デスクトップ\wildfly-35.0.1.Final"