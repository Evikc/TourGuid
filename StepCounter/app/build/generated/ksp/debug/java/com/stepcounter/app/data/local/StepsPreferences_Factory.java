package com.stepcounter.app.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class StepsPreferences_Factory implements Factory<StepsPreferences> {
  private final Provider<Context> contextProvider;

  public StepsPreferences_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public StepsPreferences get() {
    return newInstance(contextProvider.get());
  }

  public static StepsPreferences_Factory create(Provider<Context> contextProvider) {
    return new StepsPreferences_Factory(contextProvider);
  }

  public static StepsPreferences newInstance(Context context) {
    return new StepsPreferences(context);
  }
}
