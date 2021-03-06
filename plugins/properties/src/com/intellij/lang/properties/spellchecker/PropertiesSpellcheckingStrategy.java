/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.properties.spellchecker;

import com.intellij.lang.properties.psi.impl.PropertyImpl;
import com.intellij.lang.properties.psi.impl.PropertyValueImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.inspections.PropertiesSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import org.jetbrains.annotations.NotNull;


public class PropertiesSpellcheckingStrategy extends SpellcheckingStrategy {
  private Tokenizer<PropertyValueImpl> myPropertyValueTokenizer = TokenizerBase.create(PlainTextSplitter.getInstance());
  private Tokenizer<PropertyImpl> myPropertyTokenizer = new MyPropertyTokenizer();
  
  @NotNull
  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof PropertyValueImpl) {
      return myPropertyValueTokenizer;
    }
    if (element instanceof PropertyImpl) {
      return myPropertyTokenizer;
    }
    return super.getTokenizer(element);
  }

  private static class MyPropertyTokenizer extends Tokenizer<PropertyImpl> {
    public void tokenize(@NotNull PropertyImpl element, TokenConsumer consumer) {
      String key = element.getKey();
      consumer.consumeToken(element, key, true, 0, TextRange.allOf(key), PropertiesSplitter.getInstance());
    }
  }
}
