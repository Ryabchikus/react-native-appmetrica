require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-appmetrica"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-appmetrica
                   DESC
  s.homepage     = "https://github.com/sashablokhin/react-native-appmetrica"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Alexander Blokhin" => "a.blokhin@rambler-co.ru" }
  s.platforms    = { :ios => "10.0" }
  s.source       = { :git => "https://github.com/sashablokhin/react-native-appmetrica.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  # ...
  # s.dependency "..."
end

