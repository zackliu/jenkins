#! /bin/bash 
resourceContainerUrl="https://opbuildstoragesandbox2.blob.core.windows.net/opps1container"
DefaultEntryPoint="op"
UpdateNugetExe="false"
UpdateNugetConfig="true"
now=$(date +"%T")

function DownloadFile
{
    if [ ! -d "$PWD/Nuget/" ]; then
        mkdir -p $PWD/Nuget/
    fi
    wget -O $1 $2
}

function GeneratePackagesConfig
{
    cat>>$1<<EOF
<?xml version="1.0" encoding="utf-8"?>
<packages>
EOF

}

function RestorePackage
{
    if [ ! -d $3 ]; then
        mkdir -p $3
    fi

    if [ ! mono $1 restore $2 -PackagesDirectory $3 -ConfigFile $4 ]; then
        "[$now] Restore entry-point package failed" 1>&2
        exit 1
    fi
}



# Step-1: Download Nuget tools and nuget config
echo "[$now]Download Nuget tool and config"
nugetConfigSource="$resourceContainerUrl"'/Tools/Nuget/Nuget.Config'
nugetExeSource="$resourceContainerUrl"'/Tools/Nuget/nuget.exe'

nugetConfigDestination="$PWD/Nuget/Nuget.Config"
nugetExeDestination="$PWD/Nuget/nuget.exe"

DownloadFile $nugetExeDestination $nugetExeSource
DownloadFile $nugetConfigDestination $nugetConfigSource

# Step-2 Create packages.config for entry-point package





echo "[$now]Create packages.config for entry-point package"
packagesDestination="$PWD/packages.config"

if [ -f $packagesDestination ]; then
    rm $packagesDestination
fi


cat>$packagesDestination<<EOF
<?xml version="1.0" encoding="utf-8"?>
<packages>
<package id="docfx.msbuild" version="2.0.1" targetFramework="net45" />
</packages>
EOF

# Step-3 Restore entry-point package
echo "[$now]Restore opbuild package:"

packagesDestination="$PWD/packages.config"
packagesDirectory="$PWD/packages"
RestorePackage $nugetExeDestination $packagesDestination $packagesDirectory $nugetConfigDestination