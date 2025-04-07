import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        App_iosKt.doInitApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
