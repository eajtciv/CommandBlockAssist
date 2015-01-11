package com.github.eajtciv.commandblockassist.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
/**
 * リフレクションの支援になる機能を提供します。
 * @author eajtciv
 */
public class ReflectUtl {



	/**
	 * コンストラクタを探します。
	 * @param source コンストラクタを探すクラス
	 * @throws NoSuchMethodException
	 */
	public static Constructor<?> getConstructor(Class<?> source, Object[] args) throws NoSuchMethodException {
		for (Constructor<?> constructor : source.getConstructors()){
			if (isInstances(constructor.getParameterTypes(), args)){
					return constructor;
			}
		}
		throw new NoSuchMethodException(source.getName()+" "+Arrays.asList(getClassArray(args)).toString());
	}



	/**
	 * メソッドを名前で探します。
	 * @param source メソッドを探すクラス
	 * @param name メソッド名
	 * @throws NoSuchMethodException
	 */
	public static Method getMethodByName(Class<?> source, String name) throws NoSuchMethodException {
		for (Method method : source.getDeclaredMethods()){
			if (method.getName().equals(name)){
				return method;
			}
		}
		throw new NoSuchMethodException(source.getName()+" "+name);
	}



	/**
	 * メソッドを探します。
	 * @param source メソッドを探すクラス
	 * @param name メソッド名
	 * @param args メソッドに渡す引数
	 * @throws NoSuchMethodException
	 */
	public static Method getMethod(Class<?> source, String name, Object... args) throws NoSuchMethodException {
		for (Method method : source.getDeclaredMethods()){
			if (method.getName().equals(name) && isInstances(method.getParameterTypes(), args)){
				return method;
			}
		}
		throw new NoSuchMethodException(Arrays.asList(getClassArray(args)).toString()+" "+name);
	}



	/**
	 * フォールドを探します。
	 * @param source フォールドを探すクラス
	 * @param name フィールド名
	 * @throws NoSuchFieldException
	 */
	public static Field getField(Class<?> source, String name) throws NoSuchFieldException {
		for (Field field : source.getDeclaredFields()){
			if (field.getName().equals(name)){
				return field;
			}
		}

		for (Field field : source.getFields()){
			if (field.getName().equals(name)){
				return field;
			}
		}

		Class<?> superClass = source.getSuperclass();
		if(superClass != null){
			Field field = getField(superClass, name);
			if(field != null){
				return field;
			}
			throw new NoSuchFieldException(source.getName()+" "+name);
		}
		return null;
	}



	/**
	 * フォールドを探します。
	 * @param source フォールドを探すクラス
	 * @param name フィールド名
	 * @param accessible アクセスフラグ
	 * @throws NoSuchFieldException
	 */
	public static Field getField(Class<?> source, String name, boolean accessible) throws NoSuchFieldException {
		Field field = ReflectUtl.getField(source, name);
		field.setAccessible(accessible);
		return field;
	}



	/**
	 * 値を設定します。
	 * @param instance インスタンス
	 * @param name フィールド名
	 * @param value 値
	 * @return 成功した場合trueが帰ります
	 */
	public boolean setValue(Object instance, String name, Object value){

		try {
			Field field = ReflectUtl.getField(instance.getClass(), name, true);
			field.set(instance, value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}



	/**
	 * 値を取得します。
	 * @param instance インスタンス
	 * @param name フィールド名
	 * @return 失敗した場合nullが帰ります。
	 */
	public static Object getValue(Class<?> source, Object instance, String name){
		try {
			return ReflectUtl.getField(source, name, true).get(instance);
		} catch (Exception e) {
			return null;
		}
	}



	/**
	 * 値を取得します。
	 * @param instance インスタンス
	 * @param name フィールド名
	 * @return 失敗した場合nullが帰ります。
	 */
	public static Object getValue(Object instance, String name){
		return ReflectUtl.getValue(instance.getClass(), instance, name);
	}



	/**
	 * 値を取得します。
	 * @param instance インスタンス
	 * @param name フィールド名
	 * @param source 取得するクラス
	 * @return 失敗した場合nullが帰ります。
	 */
	public static <T> T getValue(Object instance, String name, Class<T> source){
		Object obj = ReflectUtl.getValue(instance, name);
		if(obj != null && source.isInstance(obj)){
			return source.cast(obj);
		}
		return null;
	}



	/**
	 * メソッドを実行します。
	 * @param instance インスタンス
	 * @param method メソッド名
	 * @param args メソッドに渡す引数
	 * @return 例外が起こらなければ成功・・・
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object invoke(Object instance, String method, Object... args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method targetMethod = ReflectUtl.getMethod(instance.getClass(), method, args);
		return invoke(instance, targetMethod, args);
	}



	/**
	 * メソッドを実行します。
	 * @param instance インスタンス
	 * @param method メソッド名
	 * @param args メソッドに渡す引数
	 * @return 例外が起こらなければ成功・・・
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object invoke(Object instance, Method method, Object... args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		method.setAccessible(true);
		return method.invoke(instance, args);
	}



	/**
	 * クラスの配列を渡します。
	 */
	public static Class<?>[] getClassArray(Object... args){
		Class<?>[] results = new Class<?>[args.length];
		for(int i=0; i < args.length; i++){
			results[i] = args[i].getClass();
		}
		return results;
	}



	/**
	 * 引数のすべてが実装しているか調べる
	 */
	private static boolean isInstances(Class<?>[] source, Object... args){
		if(source.length != args.length){
			return false;
		}
		for(int i=0; i < source.length; i++){
			if((source[i] == null || args[i] == null) || (source[i].isInstance(args[i]) == false)){
				return false;
			}
		}
		return true;
	}

}
