package com.company.gameplay;

import com.company.map.CellTypes;
import com.company.objects.GameEntity;
import com.company.objects.creatures.Hero;
import com.company.objects.creatures.Status;
import com.googlecode.lanterna.input.KeyType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.company.Main.*;
import static com.company.gameplay.GameLogic.*;

public class HeroController extends Controller
{
    public Hero hero;
    public GameEntity target;
    ArrayList<GameEntity> targets;
    private int actionCounter = 0;

    public HeroController(Hero hero) { this.hero = hero; }

    @Override
    public void step() {
        if(gameOver) cancel();

        if (hero.underCell == CellTypes.EXIT) refreshMap();

        if(hero.getExperiencePoints() >= hero.getExperiencePointsForNextLevel()) {
            hero.newLevel();
            addToLog(hero.getName() + " reached new level: " + hero.getCurrentLevel() + '!');
        }

        if(GameLogic.autoMode) {
            if(actionCounter++ >= hero.getSlowness() * 10) {
                actionCounter = 0;
                if (target != null && target.isAlive()) {
                    if (target.scanArea(1)) hero.setStatus(Status.FIGHT);
                    else if (target.scanArea(hero.getScanRadius())) hero.setStatus(Status.CHASE);
                    else hero.setStatus(Status.IDLE);
                } else hero.setStatus(Status.IDLE);

                switch (hero.getStatus()) {
                    case IDLE -> {
                        target = null;
                        hero.move();
                        targets = hero.scanAreaForTargets();
                        if (targets.size() != 0) {
                            hero.setStatus(Status.CHASE);
                            target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
                        }
                    }
                    case CHASE -> hero.moveToTarget(target);
                    case FIGHT -> target.receiveDamage(hero.getDamage());
                }
            }
        }
        else if(pressedKey != null) {
            switch (pressedKey.getKeyType()) {
                case ArrowUp -> hero.move(-1, 0);
                case ArrowRight -> hero.move(0, 1);
                case ArrowDown -> hero.move(1, 0);
                case ArrowLeft -> hero.move(0, -1);
                case Character -> {
                    if(pressedKey.getCharacter() == 'd' || pressedKey.getCharacter() == 'в')
                        hero.attackRandomTarget();
                }
            }
            pressedKey = null;
            /*if (List.of(KeyType.ArrowUp, KeyType.ArrowRight, KeyType.ArrowDown, KeyType.ArrowLeft)
                    .contains(pressedKey.getKeyType()) || (pressedKey.getKeyType() == KeyType.Character
                    && List.of('d', 'в').contains(pressedKey.getCharacter()))) pressedKey = null;*/
        }
    }
}
