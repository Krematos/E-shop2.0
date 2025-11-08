# Řešení problémů s automatickým buildem

## Problém: Backend se nekompiluje automaticky

Tento problém je obvykle způsoben nesprávným nastavením **annotation processing** pro Lombok a MapStruct.

## ✅ Řešení

### 1. IntelliJ IDEA

#### Krok 1: Povolte Annotation Processing

1. Otevřete **File → Settings** (nebo `Ctrl+Alt+S`)
2. Přejděte na **Build, Execution, Deployment → Compiler → Annotation Processors**
3. Zaškrtněte:
   - ✅ **Enable annotation processing**
   - ✅ **Obtain processors from project classpath**
4. Klikněte **Apply** a **OK**

#### Krok 2: Invalidujte cache a restartujte

1. **File → Invalidate Caches...**
2. Zaškrtněte:
   - ✅ **Clear file system cache and Local History**
   - ✅ **Clear downloaded shared indexes**
3. Klikněte **Invalidate and Restart**

#### Krok 3: Reimport Maven projektu

1. Otevřete **Maven** panel (obvykle vpravo)
2. Klikněte na ikonu **Reload All Maven Projects** (🔄)
3. Nebo: **File → Reload Project**

#### Krok 4: Zkontrolujte Maven kompilátor

1. **File → Settings → Build, Execution, Deployment → Build Tools → Maven → Runner**
2. Ujistěte se, že je vybrána správná **JRE** (Java 21)
3. Zaškrtněte **Delegate IDE build/run actions to Maven** (volitelné)

### 2. Eclipse / VS Code

#### Eclipse

1. **Project → Properties → Java Build Path → Libraries**
2. Rozbalte **Maven Dependencies**
3. Zkontrolujte, že jsou přítomny:
   - `lombok-1.18.34.jar`
   - `mapstruct-1.6.0.jar`
   - `mapstruct-processor-1.6.0.jar`

4. **Project → Clean...** → Vyberte projekt → **Clean**

#### VS Code

1. Nainstalujte rozšíření:
   - **Extension Pack for Java**
   - **Lombok Annotations Support for VS Code**

2. Otevřete Command Palette (`Ctrl+Shift+P`)
3. Spusťte: **Java: Clean Java Language Server Workspace**
4. Restartujte VS Code

### 3. Manuální build přes Maven

Pokud IDE build nefunguje, použijte Maven přímo:

```bash
# Vyčistit a znovu sestavit
mvn clean compile

# Nebo kompletní build
mvn clean install

# Spuštění aplikace
mvn spring-boot:run
```

### 4. Kontrola generovaných tříd

MapStruct generuje implementace mapperů. Zkontrolujte, zda existují:

```
target/generated-sources/annotations/org/example/mapper/
├── OrderMapperImpl.java
├── ProductMapperImpl.java
├── UserMapperImpl.java
└── OrderItemMapperImpl.java
```

Pokud tyto soubory neexistují, annotation processing nefunguje správně.

### 5. Kontrola pom.xml

Ujistěte se, že `pom.xml` obsahuje:

```xml
<annotationProcessorPaths>
    <!-- Lombok PRVNÍ -->
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <!-- MapStruct -->
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
    <!-- Lombok-MapStruct binding -->
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
        <version>0.2.0</version>
    </path>
</annotationProcessorPaths>
```

## 🔍 Diagnostika

### Kontrola, zda annotation processing funguje

1. Otevřete třídu s `@Mapper` anotací (např. `OrderMapper.java`)
2. Zkuste použít **Go to Implementation** (`Ctrl+Alt+B` v IntelliJ)
3. Měla by se otevřít generovaná třída `OrderMapperImpl.java`

Pokud se třída neotevře, annotation processing nefunguje.

### Kontrola Lombok

1. Otevřete třídu s `@Data` nebo `@Getter` (např. `User.java`)
2. Zkuste použít getter/setter metody
3. Pokud IDE hlásí chybu "cannot resolve method", Lombok nefunguje

### Logy kompilace

Zkontrolujte výstup kompilace pro chyby:

```bash
mvn clean compile -X
```

Hledejte zprávy typu:
- `[INFO] annotation processor org.mapstruct.ap.MappingProcessor`
- `[INFO] annotation processor lombok.launch.AnnotationProcessorHider$AnnotationProcessor`

## ⚠️ Časté problémy

### Problém: "Cannot find symbol" pro MapStruct mappery

**Řešení:**
1. Zkontrolujte, že `mapstruct-processor` je v `annotationProcessorPaths`
2. Spusťte `mvn clean compile`
3. Restartujte IDE

### Problém: Lombok gettery/settery nejsou viditelné

**Řešení:**
1. Nainstalujte **Lombok plugin** pro vaše IDE
2. Povolte annotation processing
3. Restartujte IDE

### Problém: "Multiple annotation processors found"

**Řešení:**
- Ujistěte se, že Lombok je PRVNÍ v `annotationProcessorPaths`
- Odstraňte duplicitní závislosti

### Problém: Build funguje v Mavenu, ale ne v IDE

**Řešení:**
1. Zkontrolujte IDE nastavení annotation processing
2. Zkontrolujte, že IDE používá stejnou JRE jako Maven
3. Invalidujte cache a restartujte IDE

## 📝 Kontrolní seznam

- [ ] Annotation processing je povoleno v IDE
- [ ] Lombok plugin je nainstalován
- [ ] Maven projekt je správně naimportován
- [ ] `pom.xml` obsahuje správné `annotationProcessorPaths`
- [ ] Java 21 je správně nastavena
- [ ] Cache byla invalidována
- [ ] IDE bylo restartováno
- [ ] Generované třídy existují v `target/generated-sources/annotations/`

## 🆘 Pokud nic nepomůže

1. **Zkuste čistý build:**
   ```bash
   mvn clean
   rm -rf target/
   mvn compile
   ```

2. **Zkontrolujte verze:**
   - Java: `java -version` (mělo by být 21)
   - Maven: `mvn -version`

3. **Zkuste jiné IDE:**
   - Pokud používáte IntelliJ, zkuste VS Code nebo Eclipse
   - Nebo naopak

4. **Kontaktujte podporu:**
   - Zkontrolujte logy: `mvn clean compile -X > build.log 2>&1`
   - Sdílejte `build.log` a `pom.xml`

---

**Poznámka:** Po každé změně v `pom.xml` je nutné:
1. Reimportovat Maven projekt
2. Invalidovat cache
3. Restartovat IDE

