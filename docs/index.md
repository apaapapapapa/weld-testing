# CDI-Unit を試してみた！【初心者向け解説】

## **🎯 はじめに**

Jakarta EE の **CDI (Context and Dependency Injection)** をテストする際、**Arquillian** を使っている方も多いと思います。  
Arquillian は **コンテナ上での結合テスト** に優れていますが、**ユニットテストを素早く実行したい場合** には、起動が遅く、セットアップが煩雑になることがあります。

そこで今回は、[**CDI-Unit**](https://github.com/cdi-unit/cdi-unit) というテストフレームワークを試してみます！  
CDI-Unit を使うと **CDI の機能を軽量な環境でテスト** でき、単体テストレベルでは、Arquillian の代替手段として有用だと見立てています。

本記事では、CDI-Unitの簡単な紹介と、個人的に試行で躓いたデータベースに対するテスト方法を解説します。

## **📌 目次**

1. [CDI-Unit とは？](#1️⃣-cdi-unit-とは)
1. [Arquillian との違い](#2️⃣-arquillian-との違い)
1. [CDI-Unit のセットアップ](#3️⃣-cdi-unit-のセットアップ)
1. [実際に CDI のテストを書いてみる](#4️⃣-実際に-cdi-のテストを書いてみる)
1. [まとめ](#5️⃣-まとめ)

## **1️⃣ CDI-Unit とは？**

[**CDI-Unit**](https://github.com/cdi-unit/cdi-unit) は、Jakarta EE の **CDI** を **軽量な環境でテストするためのフレームワーク** です。

通常、CDI をテストする場合、**Weld** や **Payara Micro** のような CDI コンテナを起動する必要があります。しかし、CDI-Unit を使えば、**JUnit 上で CDI を簡単に有効化** し、素早くテストを実行できます。

### **✅ CDI-Unit のメリット**

- **CDI 環境を素早く起動できる**
- **Arquillian のようなコンテナデプロイが不要**
- **`@Inject` をそのまま利用可能**
- **Weld（Jakarta EE における 標準の CDI 実装） との互換性がある**
- **JUnit5 に対応**

---

## **2️⃣ Arquillian との違い**

Arquillian を使っている人向けに、CDI-Unit の特徴を整理すると、次のようになります。

| **項目** | **Arquillian** | **CDI-Unit** |
|------|------------|------------|
| **目的** | **統合テスト**（実際のアプリを動かす） | **ユニットテスト**（CDI のみを対象） |
| **コンテナ** | **必要（WildFly, Payara など）** | **不要（Weld SE で CDI のみ起動）** |
| **テスト速度** | **遅い（コンテナ起動が必要）** | **高速（JUnit のみで実行）** |
| **CDI のサポート** | **広い（`@Inject`, `@PersistenceContext`, `@Transactional` など可）** | **部分的（`@Inject`、`@EJB`、`@Resource` は可）** |
| **DB テスト** | **可能（JPA + トランザクション管理）** | **Deltaspikeのみ可能（JPA + トランザクション管理）** |

CDI を **軽量なユニットテストで試したいとき** は、CDI-Unit の方が適しています。  
**実際のコンテナ環境で動作確認する場合** は、Arquillian が向いています。

また、**CDI-Unitでは、CDIのサポートが制限されている**ことに注意が必要です。

特に、Deltaspikeを利用していないアプリケーションに対しては、JPA (`@PersistenceContext`、`@Transactional`) の完全なサポートがないので、`@PersistenceContext` の `EntityManager` を直接利用できません。

代わりに @Produces で EntityManager を手動で提供する必要があります（[実際に CDI のテストを書いてみる](#4️⃣-実際に-cdi-のテストを書いてみる)参照）。

---

## **3️⃣ CDI-Unit のセットアップ**

基本的には、[公式ガイド](https://cdi-unit.github.io/cdi-unit)と[サンプルコード](https://github.com/cdi-unit/cdi-unit)の内容に従います。

### **📦 依存関係の追加**

Maven プロジェクトで `pom.xml` に以下の依存関係を追加します。

```xml
<dependencies>
    <!-- CDI-Unit -->
    <dependency>
        <groupId>io.github.cdi-unit</groupId>
        <artifactId>cdi-unit</artifactId>
        <version>5.0.0-EA7</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.jboss.weld.se</groupId>
        <artifactId>weld-se-core</artifactId>
        <version>5.1.5.Final</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.jboss.weld.module</groupId>
        <artifactId>weld-web</artifactId>
        <version>5.1.5.Final</version>
        <scope>test</scope>
    </dependency>

    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.12.1</version>
        <scope>test</scope>
    </dependency>

    <!-- assertj -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>4.0.0-M1</version>
        <scope>test</scope>
    </dependency>

    <!-- Jakarta EE関連の依存関係 -->

</dependencies>

```

## 4️⃣ 実際に CDI のテストを書いてみる

CDI-Unit を使って、Arquillian なしで CDI のテストを動かす方法 を紹介します。

### 🛠️ テスト対象のクラス

```java
package com.example.task;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@Stateless
public class TaskRepository {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final EntityManager em;

    @Inject
    public TaskRepository(EntityManager em) {
        this.em = em;
    }

    public List<Task> findAll() {
        logger.info("Getting all task");
        return em.createQuery("SELECT c FROM task c", Task.class).getResultList();
    }

    public Task create(Task task) {
        logger.info("Creating task" + task.getTitle());
        em.persist(task);
        return task;
    }

    public Optional<Task> findById(Long id) {
        logger.log(Level.INFO, "Getting task by id {0}", id);
        return Optional.ofNullable(em.find(Task.class, id));
    }

    public void delete(Long id) {
        logger.log(Level.INFO, "Deleting task by id {0}", id);
        var task = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        em.remove(task);
    }

    public Task update(Long id, String title) {
        logger.log(Level.INFO, "Updating task {0}", title);
        Task ref = em.getReference(Task.class, id);
        ref.setTitle(title);
        return em.merge(ref);
    }
    
}
```

<details>
<summary>その他関連ファイル</summary>

### Entityクラス

```java
package com.example.task;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column
    private String title;

}
```

### EntityManager生成クラス

```java
package com.example.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class EntityManagerProducer {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("TaskPersistenceUnit");

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }
}
```

### テスト用のデータベース（persistence.xml）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0" xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">

    <persistence-unit name="DefaultPersistenceUnit">
        <class>com.example.deltaspike.Human</class>
        <properties>
            <property name="hibernate.connection.url" value="jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" />
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"></property>
            <property name="hibernate.connection.driver_class" value="org.h2.Driver" />
            <property name="hibernate.connection.password" value="admin" />
            <property name="hibernate.connection.username" value="admin" />
            <property name="hibernate.hbm2ddl.auto" value="create" />
        </properties>
    </persistence-unit>

</persistence>
```

</details>

### 🧪 CDI-Unit を使ったテスト

```java
package com.example.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.cdiunit.junit5.CdiJUnit5Extension;
import io.github.cdiunit.AdditionalClasses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ExtendWith(CdiJUnit5Extension.class)
@AdditionalClasses({TaskService.class, EntityManagerProducer.class})
class TaskServiceTest {

    @Inject
    private TaskService taskService;

    @Inject
    private EntityManager entityManager;

    @Test
    @Transactional
    void testSaveTask() {
        Task task = new Task();
        task.setTitle("Test Task");

        assertDoesNotThrow(() -> taskService.saveTask(task));

        List<Task> tasks = taskService.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }
}
```

### 💡 Arquillian との違い

## 5️⃣ まとめ

CDI-Unit を使うことで、Arquillian を使わずに CDI のユニットテストを軽量に実行 できます。

### 📌 この記事のポイント

✅ Arquillian のようなコンテナ不要で CDI のテストができる
✅ @Inject をそのまま利用可能
✅ CDI の機能を簡単にモックしながらテストできる
✅ 統合テストは Arquillian、ユニットテストは CDI-Unit という使い分けが可能

CDI-Unit は、CDI の簡単なロジックを素早くテストするのに最適 です。
普段 Arquillian を使っている方も、軽量なテスト環境が欲しいときは CDI-Unit を試してみてください！🚀

### 📝 参考資料

* [CDI-Unit GitHub リポジトリ](https://github.com/cdi-unit/cdi-unit)