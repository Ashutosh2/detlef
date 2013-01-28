package at.ac.tuwien.detlef.fragments;

import android.content.Context;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.settings.GpodderSettings;

public class MockDependencyAssistant extends Singletons {
    @Override
    public GpodderSettings getGpodderSettings(Context context) {
        return new MockGpodderSettings();
    }
}
