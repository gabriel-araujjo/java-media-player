package br.imd.mediaplayer.model.annotation.processor;

import br.imd.mediaplayer.model.annotation.Model;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Processor for Model annotation
 *
 * This class generate all necessary DAO classes
 */
public class ModelProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(Model.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            processAnnotations(roundEnvironment);
        }
        return true;
    }

    private void processAnnotations(RoundEnvironment roundEnv) {
        // Iterate over all @Model annotated elements
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Model.class)) {
            if (isValidElement(annotatedElement)) {
                String daoClassName = annotatedElement.getSimpleName().toString();
                String daoPackage = elementUtils.getPackageOf(annotatedElement).getQualifiedName().toString();
                String daoFile = annotatedElement.getAnnotation(Model.class).value();

                try {
                    generateDAO(daoPackage, daoClassName, daoFile);
                } catch (IOException e) {
                    messager.printMessage(ERROR, "Can't generate class " + daoPackage + "." + daoClassName);
                }
            } else {
                return; // Exit processing
            }
        }
    }

    private boolean isValidElement(final Element annotatedElement) {

        // check whether it is a class
        if (annotatedElement.getKind() != ElementKind.CLASS) {
            error(annotatedElement, "Only classes can be annotated with @%s",
                    Model.class.getSimpleName());
            return false;
        }

        Model annotation = annotatedElement.getAnnotation(Model.class);
        String file = annotation.value();

        // check whether it has file parameter on annotation
        if (file.isEmpty()) {
            error(annotatedElement, "@%s annotation requires file parameter", Model.class.getSimpleName());
            return false;
        }

        // check whether it is concrete class
        if (annotatedElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(annotatedElement, "Only concrete classes can be annotated with @%s", Model.class.getSimpleName());
            return false;
        }

        // check whether it inherit from AbstractModel class
        TypeElement currentElement = (TypeElement) annotatedElement;

        while (true) {
            TypeMirror superClassType = currentElement.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE) {
                // Basis class (java.lang.Object) reached, so exit
                error(annotatedElement, "The class %s annotated with @%s must inherit from %s",
                        ((TypeElement) annotatedElement).getQualifiedName().toString(), Model.class.getSimpleName(),
                        "AbstractModel");
                return false;
            }

            if (superClassType.toString().equals("br.imd.mediaplayer.model.AbstractModel")) {
                // Required super class found
                break;
            }

            currentElement = (TypeElement) typeUtils.asElement(superClassType);
        }

        // Check if an empty constructor is given
        for (Element enclosed : annotatedElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0) {
                    // Found an empty constructor
                    return true;
                }
            }
        }

        // No empty constructor found
        error(annotatedElement, "The class %s must provide an empty default constructor",
                ((TypeElement) annotatedElement).getQualifiedName().toString());
        return false;
    }

    private void generateDAO(String packageName, String className, String fileName) throws IOException {


        JavaFileObject file = filer.createSourceFile(packageName + "." + className + "DAO", elementUtils.getTypeElement(packageName + "." + className));

        messager.printMessage(Diagnostic.Kind.NOTE, String.format("Generating class %s", file.toUri()));

        Writer writer = file.openWriter();

        writer
                .append(String.format("package %s;\n\n", packageName))

                .append("import br.imd.mediaplayer.dao.DAO;\n\n")

                .append(String.format("public class %sDAO extends DAO<%s> {\n\n", className, className))

                .append(String.format("    private %sDAO() { super(\"%s\"); }\n\n", className, fileName))

                .append(String.format("    private static final %sDAO INSTANCE = new %sDAO();\n\n", className, className))

                .append(String.format("    public static %sDAO getInstance() {\n", className))
                .append("        return INSTANCE;\n")
                .append("    }\n\n")

                .append("}");

        writer.close();

    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                ERROR,
                String.format(msg, args),
                e);
    }

}
