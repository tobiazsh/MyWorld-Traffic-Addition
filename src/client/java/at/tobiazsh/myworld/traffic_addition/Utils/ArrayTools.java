package at.tobiazsh.myworld.traffic_addition.Utils;


/*
 * @created 09/10/2024 (DD/MM/YYYY) - 20:53
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import java.util.List;

public class ArrayTools {

	public static <T> List<T> moveElementUpBy(List<T> list, int index, int moveUpBy) {
		for (int i = 0; i < moveUpBy; i++) {
			T switching = list.get(index); // Main Object (Subject)
			T object = list.get(index - 1); // Object (!= Subject)

			list.set(index - 1, switching);
			list.set(index, object);

			index--;
		}

		return list;
	}

	public static <T> List<T> moveElementDownBy(List<T> list, int index, int moveDownBy) {
		for (int i = 0; i < moveDownBy; i++) {
			T switching = list.get(index); // Main Object (Subject)
			T object = list.get(index + 1); // Object (!= Subject)

			list.set(index + 1, switching);
			list.set(index, object);

			index++;
		}

		return list;
	}

	public static <T> List<T> moveElementUpTo(List<T> list, int index, int newIndex) {
		int moveUpBy = index - newIndex;
		return moveElementUpBy(list, index, moveUpBy);
	}

	public static <T> List<T> moveElementDownTo(List<T> list, int index, int newIndex) {
		int moveDownBy = newIndex - index;
		return moveElementDownBy(list, index, moveDownBy);
	}
}
