## Comment lancer les tests:

1. Installer Maestro : https://maestro.mobile.dev/getting-started/installation
2. Avoir l'application installée sur un émulateur ou appareil Android
3. Le MockWebServer doit être configuré pour retourner toujours Pikachu

## Exécution des tests

Parfois j'avais des bugs en me disant que ça ne fonctionnait pas. 

Je l'ai reglé avec :
```bash
adb shell am start -n org.example.project/.MainActivity 
# et si ça marchait tjrs pas
adb kill-server 
adb start-server
```

Pour exécuter un test spécifique :
```bash
maestro test maestro/scenario1-bon-pokemon.yaml
```

Pour exécuter tous les tests :
```bash
maestro test maestro/
```

Attention ils sont très long car 10 scénarios assez similaires mais avec quelques différences donc mieux vaut les exécuter séparément :)

Pour exécuter avec un appareil spécifique :
```bash
maestro test maestro/scenario1-bon-pokemon.yaml --device <device-id>
```
