// Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
// or more contributor license agreements. Licensed under the Elastic License
// 2.0; you may not use this file except in compliance with the Elastic License
// 2.0.
package org.elasticsearch.xpack.esql.expression.function.scalar.spatial;

import java.lang.IllegalArgumentException;
import java.lang.Override;
import java.lang.String;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.DoubleBlock;
import org.elasticsearch.compute.data.LongBlock;
import org.elasticsearch.compute.data.LongVector;
import org.elasticsearch.compute.data.Page;
import org.elasticsearch.compute.operator.DriverContext;
import org.elasticsearch.compute.operator.EvalOperator;
import org.elasticsearch.compute.operator.Warnings;
import org.elasticsearch.core.Releasables;
import org.elasticsearch.geometry.Point;
import org.elasticsearch.xpack.esql.core.tree.Source;

/**
 * {@link EvalOperator.ExpressionEvaluator} implementation for {@link StDistance}.
 * This class is generated. Do not edit it.
 */
public final class StDistanceCartesianPointDocValuesAndConstantEvaluator implements EvalOperator.ExpressionEvaluator {
  private final Source source;

  private final EvalOperator.ExpressionEvaluator leftValue;

  private final Point rightValue;

  private final DriverContext driverContext;

  private Warnings warnings;

  public StDistanceCartesianPointDocValuesAndConstantEvaluator(Source source,
      EvalOperator.ExpressionEvaluator leftValue, Point rightValue, DriverContext driverContext) {
    this.source = source;
    this.leftValue = leftValue;
    this.rightValue = rightValue;
    this.driverContext = driverContext;
  }

  @Override
  public Block eval(Page page) {
    try (LongBlock leftValueBlock = (LongBlock) leftValue.eval(page)) {
      LongVector leftValueVector = leftValueBlock.asVector();
      if (leftValueVector == null) {
        return eval(page.getPositionCount(), leftValueBlock);
      }
      return eval(page.getPositionCount(), leftValueVector);
    }
  }

  public DoubleBlock eval(int positionCount, LongBlock leftValueBlock) {
    try(DoubleBlock.Builder result = driverContext.blockFactory().newDoubleBlockBuilder(positionCount)) {
      position: for (int p = 0; p < positionCount; p++) {
        if (leftValueBlock.isNull(p)) {
          result.appendNull();
          continue position;
        }
        if (leftValueBlock.getValueCount(p) != 1) {
          if (leftValueBlock.getValueCount(p) > 1) {
            warnings().registerException(new IllegalArgumentException("single-value function encountered multi-value"));
          }
          result.appendNull();
          continue position;
        }
        try {
          result.appendDouble(StDistance.processCartesianPointDocValuesAndConstant(leftValueBlock.getLong(leftValueBlock.getFirstValueIndex(p)), this.rightValue));
        } catch (IllegalArgumentException e) {
          warnings().registerException(e);
          result.appendNull();
        }
      }
      return result.build();
    }
  }

  public DoubleBlock eval(int positionCount, LongVector leftValueVector) {
    try(DoubleBlock.Builder result = driverContext.blockFactory().newDoubleBlockBuilder(positionCount)) {
      position: for (int p = 0; p < positionCount; p++) {
        try {
          result.appendDouble(StDistance.processCartesianPointDocValuesAndConstant(leftValueVector.getLong(p), this.rightValue));
        } catch (IllegalArgumentException e) {
          warnings().registerException(e);
          result.appendNull();
        }
      }
      return result.build();
    }
  }

  @Override
  public String toString() {
    return "StDistanceCartesianPointDocValuesAndConstantEvaluator[" + "leftValue=" + leftValue + ", rightValue=" + rightValue + "]";
  }

  @Override
  public void close() {
    Releasables.closeExpectNoException(leftValue);
  }

  private Warnings warnings() {
    if (warnings == null) {
      this.warnings = Warnings.createWarnings(
              driverContext.warningsMode(),
              source.source().getLineNumber(),
              source.source().getColumnNumber(),
              source.text()
          );
    }
    return warnings;
  }

  static class Factory implements EvalOperator.ExpressionEvaluator.Factory {
    private final Source source;

    private final EvalOperator.ExpressionEvaluator.Factory leftValue;

    private final Point rightValue;

    public Factory(Source source, EvalOperator.ExpressionEvaluator.Factory leftValue,
        Point rightValue) {
      this.source = source;
      this.leftValue = leftValue;
      this.rightValue = rightValue;
    }

    @Override
    public StDistanceCartesianPointDocValuesAndConstantEvaluator get(DriverContext context) {
      return new StDistanceCartesianPointDocValuesAndConstantEvaluator(source, leftValue.get(context), rightValue, context);
    }

    @Override
    public String toString() {
      return "StDistanceCartesianPointDocValuesAndConstantEvaluator[" + "leftValue=" + leftValue + ", rightValue=" + rightValue + "]";
    }
  }
}
