import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BracketsTest {
    private val balancedBrackets = listOf('(' to ')', '[' to ']', '{' to '}')

    @Test
    fun `empty Brackets should be valid`() {
        assertTrue(Brackets().valid)
    }

    @Test
    fun `non-empty Brackets should not be valid`() {
        val root = Brackets()

        root.push('[')

        assertFalse(root.valid)
    }

    @Test
    fun `balanced Brackets should be valid`() {
        val root = Brackets()

        root.randomBalanced(100)

        assertTrue(root.valid)
    }

    @Test
    fun `unbalanced Brackets should not be valid`() {
        val root = Brackets()

        root.push('(')
        root.push('[')
        root.push('{')
        root.push(']')
        root.push('}')
        root.push(')')

        assertFalse(root.valid)
    }

    @Test
    fun `toString of a balanced Brackets should be empty`() {
        val root = Brackets()

        root.randomBalanced(100)

        assertEquals("", root.toString())
    }

    @Test
    fun `toString of an unbalanced Brackets`() {
        val root = Brackets()

        root.push('(')
        root.push('[')
        root.push('{')

        assertEquals("([{", root.toString())
    }

    private fun Brackets.randomBalanced(number: Int) {
        val (opening, closing) = balancedBrackets.random()

        this.push(opening)
        if (number > 0) {
            this.randomBalanced(number - 1)
        }
        this.push(closing)
    }
}
