package com.l2jaln.gameserver.model;

import com.l2jaln.gameserver.data.RecipeTable;

public class L2ManufactureItem
{
	private final int _recipeId;
	private final int _cost;
	private final boolean _isDwarven;
	
	public L2ManufactureItem(int recipeId, int cost)
	{
		_recipeId = recipeId;
		_cost = cost;
		
		_isDwarven = RecipeTable.getInstance().getRecipeList(_recipeId).isDwarvenRecipe();
	}
	
	public int getRecipeId()
	{
		return _recipeId;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public boolean isDwarven()
	{
		return _isDwarven;
	}
}