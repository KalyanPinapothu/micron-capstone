# Contributing

Thanks for helping build out the Micron platform. This is a shared repo across business-unit teams (Retail Banking, Corporate Banking, Wealth Management, Digital Payments) plus platform/DevOps — please follow these conventions so changes stay easy to review and safe to deploy.

## Branching Model

- `main` — always deployable; protected, merges only via reviewed PR
- `develop` — integration branch for the current sprint/release
- Feature branches: `feature/<short-description>` (e.g. `feature/compliance-region-validator`)
- Fix branches: `fix/<short-description>`
- One feature/fix per branch — keep PRs small and reviewable

## Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(core): add compliance-region validator workflow step
fix(dispatcher): close /bin/dita/* on publish tier
docs(guide): add section 9.3 custom validation extension point
chore(config): update fmdita ConfigManager for new publish plugin
```

Types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `config`

## Pull Requests

1. Branch off `develop` (or `main` for hotfixes)
2. Make your change; add/update tests where applicable (`core` module)
3. Run `mvn clean install` locally and confirm no failures
4. If you touched dispatcher rules, run the validation checklist in `dispatcher/README.md`
5. Open a PR against `develop` using the PR template — link the related section of the implementation guide if relevant
6. At least one reviewer from platform/DevOps required for changes under `ui.config/`, `dispatcher/`, or `core/`
7. At least one reviewer from the relevant business-unit team required for changes under folder/DITA profile configs

## Where to Make Common Changes

| Change | Location |
|---|---|
| New business-unit DITA/Folder profile | `ui.content/src/main/content/jcr_root/conf/micron/...` |
| Custom workflow validation step | `core/src/main/java/com/micron/bank/core/workflow/` |
| OSGi configuration | `ui.config/src/main/content/jcr_root/apps/micron/osgiconfig/` |
| ACLs / service users | `ui.content/repoinit/` |
| Dispatcher cache/security rules | `dispatcher/src/conf.dispatcher.d/` |
| Documentation updates | `docs/implementation-guide/` |

## Code Style

- Java: standard AEM/OSGi conventions (`@Component`, `@Designate`), 4-space indent
- Keep `/libs` untouched — all customizations go through `/apps` overlays
- Never commit real credentials, tokens, or production ACL data — use placeholders and document the real values in your team's secrets manager

## Reporting Issues

Use the issue templates under `.github/ISSUE_TEMPLATE/` — bug report or feature/config request. Tag with the relevant business unit and module (`core`, `dispatcher`, `config`, `docs`).
