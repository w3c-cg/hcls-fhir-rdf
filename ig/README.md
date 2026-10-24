# Semantic Web for FHIR Implementation Guide (IG)

This repository contains the source for an HL7 FHIR implementation guide about
practical applications of FHIR RDF, SPARQL, ShEx, and OWL.

## Authoring

Narrative pages are authored as Kramdown in `input/pagecontent`. Metadata, page
registration, and navigation are maintained in `sushi-config.yaml`. Page images
belong in `input/images`. Header assets and overrides belong in
`custom-template`, which derives from the official `fhir2.base.template`.

*Do not edit generated files under `fsh-generated`, `output`, `temp`, or `template`.*

The build follows the same structure as many FHIR IGs:

1. SUSHI generates the `ImplementationGuide` resource from `sushi-config.yaml`.
2. The HL7 IG Publisher renders the Markdown with `custom-template`, which
	derives from `fhir2.base.template`.
3. The generated static site and validation reports are written to `output/`.

## Local build

Install Java, Node.js/npm, and Ruby first. SUSHI and Jekyll must be on `PATH`:

```sh
npm install -g fsh-sushi
gem install jekyll bundler
```

From the repository root, download the Publisher JAR into the gitignored cache
(repeat the download when deliberately updating Publisher):

```sh
cd ig
mkdir -p input-cache
curl --fail --location --retry 3 \
	https://github.com/HL7/fhir-ig-publisher/releases/latest/download/publisher.jar \
	--output input-cache/publisher.jar
```

Build from the `ig/` directory:

```sh
sushi build .
./scripts/_genonce.sh
open output/index.html
```

Publisher rejects absolute directory paths containing spaces. Prefer a checkout in a path without spaces. Alternatively, after downloading Publisher and running SUSHI above, build a temporary copy (run from `ig/`; requires `rsync`):

```sh
build_dir=$(mktemp -d /tmp/fhir-ig.XXXXXX) &&
	rsync -a --exclude output --exclude temp --exclude template ./ "$build_dir/" &&
	sh "$build_dir/scripts/_genonce.sh" &&
	rsync -a "$build_dir/output/" output/ &&
	open output/index.html
```

Only copy the output after Publisher succeeds. The temporary directory is kept
for inspecting build logs; remove it when no longer needed. This copy does not
include Git metadata, so Publisher will warn that it cannot determine the branch
or source repository.

Note: Publisher reads the SUSHI-generated `ImplementationGuide`, not
`sushi-config.yaml`, and only auto-runs SUSHI if `input/fsh/` exists (it
doesn't here). After editing `sushi-config.yaml` (title, id, pages, menu,
etc.), run `sushi build .` before `./scripts/_genonce.sh`.

This script invokes Publisher directly, uses the normal FHIR package cache at
`~/.fhir/packages`, and permits dependency downloads. It disables terminology
service calls with `-tx n/a`; the first build still needs internet access to
download FHIR packages and templates.
Despite the tool's name, IG Publisher only generates local files under
`output/`; this script contains no upload or web-publishing step.

### Build verification

Publisher can exit successfully despite validation errors. Always inspect its
reports before treating the output as publication-ready:

```sh
open output/qa.html
cat output/qa.json
```

## Publishing

The [workflow](../.github/workflows/build-ig.yaml) validates branches and PRs.
Only successful pushes or manual runs on the upstream `gh-pages` branch deploy
the current site, after checking for zero errors, unsuppressed warnings, and
broken links. It preserves the existing site and publishes `ig/output/` at
[the IG URL](https://w3c-cg.github.io/hcls-fhir-rdf/ig/index.html).

Repository setup: select **Settings > Pages > Source > GitHub Actions**, protect
`gh-pages` with required review/checks, and restrict the `github-pages`
environment to that branch. Releases use tags `ig-v<version>` matching
`sushi-config.yaml`; publishing a GitHub Release attaches the built site ZIP
without replacing the current Pages site.

## Syntax highlighting colors

Edit the `--syntax-*` color variables at the top of
[syntax.css](custom-template/content/assets/css/syntax.css) to customize SPARQL
and RDF Turtle highlighting. The rules below the palette map both Rouge and
Prism token classes to those colors; other languages are unaffected.

The Publisher loads this stylesheet through
[the CSS include](custom-template/includes/_append.fragment-css.html).

## Why the full build is large

Turning Markdown into HTML is the small part. A conformant IG build also:

* runs SUSHI to generate the `ImplementationGuide` resource;
* loads FHIR R5 definitions and the inherited HL7 template chain;
* runs Publisher validation, cross-version handling, link checks, navigation,
  package assembly, and QA report generation; and
* normally consults package registries and a terminology server.

These runtime packages include FHIR R5 core, the HL7 base templates, Publisher
support data, cross-version definitions, and R5 extension and terminology
packages. They are build tools and reference data, not dependencies introduced
by this guide's content. Publisher manages them in the standard global cache.
No separate fast-preview command is currently included in this directory.

The Publisher cannot render a conformant guide with zero FHIR packages. The
selected FHIR core package and `fhir2.base.template` are its minimum rendering
runtime, not implementation guide dependencies declared by this project. No
other implementation guides are declared as dependencies. Usage-stat reporting
is disabled in `ig.ini`.
