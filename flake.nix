{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    nixpkgs-stable.url = "github:NixOS/nixpkgs/nixos-23.11";
  };

  outputs = { self, nixpkgs, nixpkgs-stable }:

    let
      pkgs = nixpkgs.legacyPackages.x86_64-linux;
      pkgsStable = nixpkgs-stable.legacyPackages.x86_64-linux;

      libs = (with pkgs; [
        libGL
        glfw
        stdenv.cc.cc.lib

      ]);

      java = pkgs.jdk17;
      maven = pkgsStable.maven;

      javaDeps = ([
        java
        maven
      ]);
    in
    {
      devShell.x86_64-linux = pkgs.mkShell {
        packages = [ ];
        buildInputs = libs ++ javaDeps;
        LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath libs;
        JAVA_HOME = java.home;
      };

      packages.x86_64-linux.default = maven.buildMavenPackage rec {
        pname = "legacy-dual-contouring";
        version = "main";

        src = pkgs.fetchFromGitHub {
          owner = "thinnerthinker";
          repo = pname;
          rev = version;
          hash = "sha256-I00kc1aGwsLblWNK6l80jf2GJPChAol+S90jC/lM1vQ=";
        };

        mvnHash = "sha256-6wCGiMdeSqfJk8rVKNUMv9PefWZE2xmobQYgGPSA4Xs=";

        buildInputs = libs ++ javaDeps ++ [ pkgs.makeWrapper ];

        installPhase = ''
          mkdir -p $out/jar
          cp target/legacy-dual-contouring-1.0-SNAPSHOT.jar $out/jar/

          makeWrapper ${java}/bin/java $out/bin/legacy-dual-contouring \
            --add-flags "-cp $out/jar/legacy-dual-contouring-1.0-SNAPSHOT.jar com.yallo.dual_contouring.Main" \
            --set JAVA_HOME "${java.home}" \
            --prefix LD_LIBRARY_PATH : ${pkgs.lib.makeLibraryPath libs}
        '';
      };
    };
}
