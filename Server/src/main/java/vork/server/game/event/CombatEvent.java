package vork.server.game.event;

import java.util.List;
import java.util.Random;

import vork.server.Constants;
import vork.server.game.DropTable;
import vork.server.game.container.ItemStack;
import vork.server.game.entity.Entity;
import vork.server.game.entity.Npc;
import vork.server.game.entity.Player;
import vork.server.game.skills.SkillType;

public class CombatEvent extends Event {

	private Entity attacker;
	private Entity defender;
	
	private static final Random random = new Random();
	
	public CombatEvent(Entity attacker, Entity defender) {
		this.attacker = attacker;
		this.defender = defender;
	}
	
	public static boolean checkAgainstMulticombat(Entity attacker, Entity defender) {
		if (attacker.attackingEntities.size() > 1 ||
			(attacker.attackingEntities.size() == 1 && attacker.attackingEntities.get(0) != defender)
				) {
			if (attacker instanceof Player) {
				((Player) attacker).sendServerMessage("you are already under attack.");		
			}
			return false;
		}
		if (defender.getCombatTarget() != attacker &&
			defender.getAction() instanceof CombatEvent) {
			if (attacker instanceof Player) {
				((Player) attacker).sendServerMessage("The entity is already in combat.");
			}
			return false;
		}
		if (defender.isUnderAttack()) {
			if (attacker instanceof Player) {
				((Player) attacker).sendServerMessage("The entity is already under attack.");	
			}
			return false;
		}
		
		return true;
	}
	
	public boolean ourTurn(Entity entity) {
		// It is the entity's turn if the entity is already the
		// attacker or if the other entity is not attacking back.
		return attacker == entity ||
				!(attacker.getAction() instanceof CombatEvent) ||
				attacker.getCombatTarget() != entity;
	}
	
	private int getTicksTillNextAttack() {
		// TODO: This will be based on attack speed of the entity.
		return Constants.TPS;
	}
	
	@Override
	protected void run() {
		
		if (defender.isDead() || attacker.isDead()) {
			return;
		}
		
		fight();
		
		setDelayTicks(getTicksTillNextAttack());
		
		// Swap the attacker and defender so their turns are swapped.
		if (defender.getAction() != this) {
			// No reason to swap turns because the defender is not attacking back.
			return;
		}
	
		Entity temp = attacker;
		attacker = defender;
		defender = temp;
	
	}
	
	private void fight() {
		
		boolean inRange = attacker.distanceTo(defender) <= 1;
		
		if (!inRange) {
			return;
		}
		
		int damageDelt = random.nextInt(attacker.getMaxHit() + 1);
		
		defender.damage(damageDelt);
		
		if (attacker instanceof Player) {
			Player player = (Player) attacker;
			if (damageDelt > 0) {
				player.giveXp(SkillType.MELEE, damageDelt * 5);	
			}
			
			if (defender.isDead() && defender instanceof Npc) {
				Npc deadNpc = (Npc) defender;
				DropTable dropTable = deadNpc.getDropTable();
				if (dropTable != null) {
					List<ItemStack> drops = dropTable.roll();
					for (ItemStack drop : drops) {
						player.dropItem(drop, defender.currentLocation);
					}
				}
			}
		}
		
		if (!defender.isDead() && defender instanceof Npc) {
			// Letting the npc attack back.
			if (!defender.isAttacking()) {
				defender.targetEntity(attacker);
				defender.tryToAttack();
			}
		}
	}

	@Override
	public boolean isOver() {
		return attacker.isDead() || defender.isDead();
	}
}
