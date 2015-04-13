package com.jonathonstaff.androidaccessors;

//  Created by jonstaff on 6/23/14.

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    private final PsiClass mClass;
    private final List<PsiField> mFields;

    public CodeGenerator(PsiClass psiClass, List<PsiField> fields) {
        mClass = psiClass;
        mFields = fields;
    }

    public void generate() {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(mClass.getProject());

        // TODO: remove old accessors

        List<PsiMethod> methods = new ArrayList<PsiMethod>();

        List<PsiField> fields = new ArrayList<PsiField>();

        for (PsiField field : mFields) {
            PsiMethod getter =
                    elementFactory.createMethodFromText(generateGetterMethod(field), mClass);
            PsiMethod setter =
                    elementFactory.createMethodFromText(generateSetterMethod(field), mClass);
            PsiField field = elementFactory.createFieldFromText(generateStaticField(field), mClass);
            methods.add(getter);
            methods.add(setter);
            fields.add(field);
        }

        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mClass.getProject());

        for (PsiMethod method : methods) {
            styleManager.shortenClassReferences(mClass.add(method));
        }

        for (PsiField field : fields) {
            styleManager.shortenClassReferences(mClass.add(field));
        }
    }

    private static String getGetterMethodName(PsiField field) {
      return "get" + getUpperPropertyName(field);
    }

    private static String getSetterMethodName(PsiField field) {
      return "set" + getUpperPropertyName(field);
    }

    private static String getUpperPropertyName(PsiField field) {
      StringBuilder sb2 = new StringBuilder(field.getName());

      // verify that the first character is an 'm' or an 's' and the second is uppercase
      if ((sb2.charAt(0) == 'm' || sb2.charAt(0) == 's') && sb2.charAt(1) < 97) {
          sb2.deleteCharAt(0);
      }
      sb2.setCharAt(0, Character.toUpperCase(sb2.charAt(0)));
      return sb2.toString();
    }

    private static String getLowerPropertyName(PsiField field) {
      StringBuilder sb2 = new StringBuilder(field.getName());

      // verify that the first character is an 'm' or an 's' and the second is uppercase
      if ((sb2.charAt(0) == 'm' || sb2.charAt(0) == 's') && sb2.charAt(1) < 97) {
          sb2.deleteCharAt(0);
      }
      sb2.setCharAt(0, Character.toLowerCase(sb2.charAt(0)));
      return sb2.toString();
    }

    private static String generateGetterMethod(PsiField field) {
        StringBuilder sb = new StringBuilder("public ");
        sb.append(field.getType().getPresentableText());

        sb.append(" ").append(getGetterMethodName(field)).append("() { return ").append(field.getName())
          .append("; }");
        return sb.toString();
    }

    private static String joinStrings(Array<String> strings, String joiner) {
      StringBuilder sb = new StringBuilder("");
      boolean isFirst = true;
      for (String s : strings) {
        if (!isFirst) {
          sb.append(joiner);
        }
        sb.append(s);
        isFirst = false;
      }
      return sb.toString();
    }

    private static String getStaticFieldName(PsiField field) {
      return joinStrings(getUpperPropertyName(field).toString().split("(?<!^)(?=[A-Z])"), "_");
    }

    private static boolean getNeedsThis(PsiField field) {
      StringBuilder sb2 = new StringBuilder(field.getName());
      return !((sb2.charAt(0) == 'm' || sb2.charAt(0) == 's') && sb2.charAt(1) < 97);
    }

    private static String generateStaticField(PsiField field) {
      StringBuilder sb = new StringBuilder("public static final String ");
      sb.append(getStaticFieldName(field)).append(" = ").append("\"").append(getLowerFieldName(field)).append("\"");
      return sb.toString();
    }

    private static String generateSetterMethod(PsiField field) {
        StringBuilder sb = new StringBuilder("public void set");
        StringBuilder sb2 = new StringBuilder(field.getName());

        String thisStr = getNeedsThis(field) ? "this." : "";

        String param = getLowerPropertyName(field);

        String paramUpper = getUpperPropertyName(field);

        String staticFieldName = getStaticFieldName(field);

        sb.append(paramUpper).append("(").append(field.getType().getPresentableText()).append(" ")
          .append(param).append(") { ");

        sb.append("firePropertyChanging(").append(staticFieldName).append(", ")
          .append(thisStr).append(field.getName()).append(", ").append(param).append(");");

        sb.append("firePropertyChange").append(staticFieldName).append(", ")
          .append(thisStr).append(field.getName()).append(", ").append(thisStr)
          .append(field.getName()).append(" = ").append(param).append("; }");

        return sb.toString();
    }
}
