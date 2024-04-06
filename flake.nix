{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    nixpkgs-stable.url = "github:NixOS/nixpkgs/nixos-21.05";
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

    javaDeps = ([
      pkgs.jdk17
      pkgsStable.maven
    ]);

  in {
    devShell.x86_64-linux = pkgs.mkShell {
      packages = [];
      buildInputs = libs ++ javaDeps;
      LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath libs;
      JAVA_HOME = pkgs.jdk17.home;
    };
  };
}