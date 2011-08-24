/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.rstats.function;

import java.util.Collection;

import com.opengamma.language.Data;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.function.FunctionAdapter;
import com.opengamma.language.function.FunctionVisitor;
import com.opengamma.language.function.Invoke;
import com.opengamma.language.function.Result;
import com.opengamma.rstats.data.RDataInfo;
import com.opengamma.rstats.msg.DataInfo;
import com.opengamma.rstats.msg.FunctionResult;

/**
 * Wraps a default FunctionVisitor to decorate the result messages with additional information. 
 */
public class Handler extends FunctionAdapter<UserMessagePayload, SessionContext> {

  private static final DataInfo NULL = new DataInfo();

  public Handler(final FunctionVisitor<UserMessagePayload, SessionContext> underlying) {
    super(underlying);
  }

  // FunctionVisitor

  @Override
  public UserMessagePayload visitInvoke(final Invoke message, final SessionContext data) {
    final UserMessagePayload rawResult = super.visitInvoke(message, data);
    if (rawResult instanceof Result) {
      final Result result = (Result) rawResult;
      final Collection<Data> resultData = result.getResult();
      int skip = 0;
      FunctionResult decoratedResult = null;
      for (Data resultDataItem : resultData) {
        DataInfo info = RDataInfo.getFor(resultDataItem);
        if (info != null) {
          if (decoratedResult == null) {
            decoratedResult = new FunctionResult(resultData);
          }
          while (skip > 0) {
            decoratedResult.addInfo(NULL);
            skip--;
          }
          decoratedResult.addInfo(info);
        } else {
          skip++;
        }
      }
      if (decoratedResult != null) {
        return decoratedResult;
      }
    }
    return rawResult;
  }

}