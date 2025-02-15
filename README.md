# AnnotationRelocator

## Overview

The **AnnotationRelocator** is a Java-based tool that modifies source code by relocating the `@JsonbTypeAdapter(...)` annotation from field declarations to the corresponding getter and setter methods. This tool supports fields of type `Date`, `Time`, and `Timestamp`, regardless of their variable names. Additionally, it ensures that if multiple annotations exist in the annotation block, only `@JsonbTypeAdapter(...)` is removed while preserving indentation and formatting.

## Features

- **Annotation Relocation:**
  - Removes `@JsonbTypeAdapter(...)` from the field declaration.
  - Preserves its adapter argument (e.g., `DateAdapter.class`, `TimeAdapter.class`).
  - Adds the same annotation before the corresponding getter and setter methods.
- **Universal Support:**
  - Works with any variable name.
  - Applies to `Date`, `Time`, and `Timestamp` types.
- **Indentation Preservation:**
  - Ensures that no extra spaces or blank lines remain after removing the annotation.
  - Maintains proper indentation when adding annotations to getter/setter methods.

## Requirements

- **Java Environment:**
  - JDK 8 or later (only standard libraries are used).
- **Execution Environment:**
  - Command-line interface (Terminal or Command Prompt).
  - A directory containing Java source files (all `*.java` files are processed recursively).

## Usage

### 1. Place the Source Code

Save `UpdateAnnotations.java` in any directory.

Example: `/path/to/UpdateAnnotations.java`

### 2. Compile the Script

Open a terminal or command prompt, navigate to the directory containing the script, and execute:

```sh
javac UpdateAnnotations.java
```

This will generate `UpdateAnnotations.class`.

### 3. Execute the Script

Run the script, specifying the target directory containing Java files:

```sh
java UpdateAnnotations "/path/to/target/directory"
```

_This script will recursively process all \*\*`_.java`\*_ files in the specified directory._

### 4. Verify Changes

After execution, updated file names will be displayed in the console. Use a version control system (e.g., `git diff`) or a code comparison tool to review the modifications.

## Input and Output Example

### Before Execution

```java
@MyAnnotation1
@JsonbTypeAdapter(DateAdapter.class)
@MyAnnotation2
private Date datetime;
```

### After Execution

#### **Field Declaration**

```java
@MyAnnotation1
@MyAnnotation2
private Date datetime;
```

#### **Getter and Setter Methods**

The annotation is added before the corresponding getter/setter methods:

```java
@JsonbTypeAdapter(DateAdapter.class)
public Date getDatetime() {
    return this.datetime;
}

@JsonbTypeAdapter(DateAdapter.class)
public void setDatetime(Date datetime) {
    this.datetime = datetime;
}
```

## Implementation Details

- **Regex for Extracting Annotation Blocks:**
  - Captures the annotation block preceding a field declaration.
  - Only processes fields of type `Date`, `Time`, or `Timestamp`.
- **Regex for Removing **``**:**
  - Uses `(?m)^\s*@JsonbTypeAdapter\s*\(\s*([^)]*)\s*\)\s*$\r?\n?` to match and remove complete lines.
  - Ensures no unnecessary indentation or blank lines remain.
- **Adding Annotations to Getter/Setter Methods:**
  - Dynamically constructs method names following JavaBeans conventions.
  - Maintains the original indentation of the source code.

## Notes

- **Backup Recommendation:**
  - Before running this script, create a backup or commit changes to a version control system.
- **Effect of Regular Expressions:**
  - Code formats may vary; always test in a development environment before applying changes to production code.

## License

This tool is distributed under [Your License Information]. If necessary, include a license file with the project.
