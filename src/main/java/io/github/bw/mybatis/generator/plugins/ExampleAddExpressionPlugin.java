package io.github.bw.mybatis.generator.plugins;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

public class ExampleAddExpressionPlugin extends PluginAdapter {

  @Override
  public boolean validate(List<String> list) {
    return true;
  }

  private boolean isGeneratedCriteria(InnerClass innerClass) {
    return "GeneratedCriteria".equals(innerClass.getType().getShortName()); //$NON-NLS-1$
  }

  @Override
  public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

    topLevelClass.getInnerClasses().stream()
        .filter(this::isGeneratedCriteria)
        .findFirst()
        .ifPresent(c -> addMethods(introspectedTable, c));

    return true;
  }

  private void addMethods(IntrospectedTable introspectedTable, InnerClass criteria) {
    for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
      criteria.addMethod(getSetEqualMethod(introspectedColumn, true));
      criteria.addMethod(getSetNotEqualMethod(introspectedColumn, true));
      criteria.addMethod(getSetGreaterThanMethod(introspectedColumn, true));
      criteria.addMethod(getSetGreaterThenOrEqualMethod(introspectedColumn, true));
      criteria.addMethod(getSetLessThanMethod(introspectedColumn, true));
      criteria.addMethod(getSetLessThanOrEqualMethod(introspectedColumn, true));

      if (introspectedColumn.isJdbcCharacterColumn()) {
        criteria.addMethod(getSetLikeMethod(introspectedColumn, true));
        criteria.addMethod(getSetNotLikeMethod(introspectedColumn, true));
      }

      criteria.addMethod(getSetInOrNotInMethod(introspectedColumn, true, true));
      criteria.addMethod(getSetInOrNotInMethod(introspectedColumn, false, true));
      criteria.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, true, true));
      criteria.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, false, true));

    }

  }

  private Method getSetEqualMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "EqualTo", "=", withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSetNotEqualMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "NotEqualTo", "<>", withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSetGreaterThanMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "GreaterThan", ">", withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSetGreaterThenOrEqualMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "GreaterThanOrEqualTo", ">=",
        withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSetLessThanMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "LessThan", "<", withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSetLessThanOrEqualMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "LessThanOrEqualTo", "<=",
        withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSetLikeMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "Like", "like", withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSetNotLikeMethod(IntrospectedColumn introspectedColumn, boolean withExpression) {
    return getSingleValueMethod(introspectedColumn, "NotLike", "not like", withExpression); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private Method getSingleValueMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator,
      boolean withExpression) {

    StringBuilder sb = new StringBuilder();
    sb.append(initializeAndMethodName(introspectedColumn));
    sb.append(nameFragment);

    Method method = new Method(sb.toString());
    method.setVisibility(JavaVisibility.PUBLIC);
    if (withExpression) {
      method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "expression"));
    }
    method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "value")); //$NON-NLS-1$
    method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

    sb.setLength(0);
    sb.append(initializeAddLine(introspectedColumn));
    sb.append(' ');
    sb.append(operator);
    sb.append("\", "); //$NON-NLS-1$
    sb.append("value"); //$NON-NLS-1$
    sb.append(", \""); //$NON-NLS-1$
    sb.append(introspectedColumn.getJavaProperty());
    sb.append("\");"); //$NON-NLS-1$
    if (withExpression) {
      method.addBodyLine("if (expression) {");
    }
    method.addBodyLine(sb.toString());
    if (withExpression) {
      method.addBodyLine("}");
    }
    method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$

    return method;
  }

  private String initializeAndMethodName(IntrospectedColumn introspectedColumn) {
    StringBuilder sb = new StringBuilder();
    sb.append(introspectedColumn.getJavaProperty());
    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    sb.insert(0, "and"); //$NON-NLS-1$
    return sb.toString();
  }

  private Method getSetInOrNotInMethod(IntrospectedColumn introspectedColumn, boolean inMethod,
      boolean withExpression) {
    StringBuilder sb = new StringBuilder();
    sb.append(initializeAndMethodName(introspectedColumn));
    if (inMethod) {
      sb.append("In"); //$NON-NLS-1$
    } else {
      sb.append("NotIn"); //$NON-NLS-1$
    }
    Method method = new Method(sb.toString());
    method.setVisibility(JavaVisibility.PUBLIC);
    FullyQualifiedJavaType type = FullyQualifiedJavaType.getNewListInstance();
    if (introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
      type.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType().getPrimitiveTypeWrapper());
    } else {
      type.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType());
    }

    if (withExpression) {
      method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "expression"));
    }
    method.addParameter(new Parameter(type, "values")); //$NON-NLS-1$
    method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

    sb.setLength(0);
    sb.append(initializeAddLine(introspectedColumn));
    if (inMethod) {
      sb.append(" in"); //$NON-NLS-1$
    } else {
      sb.append(" not in"); //$NON-NLS-1$
    }
    sb.append("\", values, \""); //$NON-NLS-1$
    sb.append(introspectedColumn.getJavaProperty());
    sb.append("\");"); //$NON-NLS-1$
    if (withExpression) {
      method.addBodyLine("if (expression) {");
    }
    method.addBodyLine(sb.toString());
    if (withExpression) {
      method.addBodyLine("}");
    }
    method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$

    return method;
  }

  private Method getSetBetweenOrNotBetweenMethod(IntrospectedColumn introspectedColumn, boolean betweenMethod,
      boolean withExpression) {

    StringBuilder sb = new StringBuilder();
    sb.append(initializeAndMethodName(introspectedColumn));
    if (betweenMethod) {
      sb.append("Between"); //$NON-NLS-1$
    } else {
      sb.append("NotBetween"); //$NON-NLS-1$
    }
    Method method = new Method(sb.toString());
    method.setVisibility(JavaVisibility.PUBLIC);
    FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
    if (withExpression) {
      method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "expression"));
    }
    method.addParameter(new Parameter(type, "value1")); //$NON-NLS-1$
    method.addParameter(new Parameter(type, "value2")); //$NON-NLS-1$
    method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

    sb.setLength(0);
    sb.append(initializeAddLine(introspectedColumn));
    if (betweenMethod) {
      sb.append(" between"); //$NON-NLS-1$
    } else {
      sb.append(" not between"); //$NON-NLS-1$
    }
    sb.append("\", "); //$NON-NLS-1$
    sb.append("value1, value2"); //$NON-NLS-1$
    sb.append(", \""); //$NON-NLS-1$
    sb.append(introspectedColumn.getJavaProperty());
    sb.append("\");"); //$NON-NLS-1$
    if (withExpression) {
      method.addBodyLine("if (expression) {");
    }
    method.addBodyLine(sb.toString());
    if (withExpression) {
      method.addBodyLine("}");
    }
    method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$

    return method;
  }

  private String initializeAddLine(IntrospectedColumn introspectedColumn) {
    StringBuilder sb = new StringBuilder();
    if (introspectedColumn.isJDBCDateColumn()) {
      sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
    } else if (introspectedColumn.isJDBCTimeColumn()) {
      sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
    } else if (stringHasValue(introspectedColumn.getTypeHandler())) {
      sb.append("add"); //$NON-NLS-1$
      sb.append(introspectedColumn.getJavaProperty());
      sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
      sb.append("Criterion(\""); //$NON-NLS-1$
    } else {
      sb.append("addCriterion(\""); //$NON-NLS-1$
    }

    sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
    return sb.toString();
  }

}
