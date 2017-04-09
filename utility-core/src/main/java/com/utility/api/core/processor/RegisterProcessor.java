package com.utility.api.core.processor;

import java.util.List;

public interface RegisterProcessor<T> {

    List<T> processList(ProcessContext context);

    T processSingle(ProcessContext context);
}
