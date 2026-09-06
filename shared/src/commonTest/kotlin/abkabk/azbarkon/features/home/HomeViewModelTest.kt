package abkabk.azbarkon.features.home

import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.domain.model.CatNode
import abkabk.azbarkon.domain.model.Poet
import abkabk.azbarkon.domain.model.PoetWithRootCategories
import abkabk.azbarkon.testing.FakeDailyDistichRepository
import abkabk.azbarkon.testing.FakeMemorizationRepository
import abkabk.azbarkon.testing.FakePoetRepository
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import abkabk.azbarkon.testing.runViewModelTest
import kotlin.test.Test

class HomeViewModelTest {

    @Test
    fun `loading poets updates success state`() =
        runViewModelTest {
            val repository =
                FakePoetRepository().apply {
                    poetsWithRootCategories =
                        listOf(
                            PoetWithRootCategories(
                                poet =
                                    Poet(
                                        id = 1,
                                        name = "حافظ",
                                        description = null,
                                        rootCatId = 10,
                                        imageUrl = "https://api.ganjoor.net/api/ganjoor/poet/image/hafez.png",
                                    ),
                                rootCategories =
                                    listOf(
                                        CatNode(
                                            id = 100,
                                            poetId = 1,
                                            text = "غزلیات",
                                            parentId = 10,
                                            url = "/test",
                                        ),
                                    ),
                            ),
                        )
                }
            val viewModel = HomeViewModel(repository, FakeMemorizationRepository(), FakeDailyDistichRepository())

            val state = viewModel.state.value
            assertThat(state.screenState).isInstanceOf(UiScreenState.Success::class)
            assertThat(state.poets.size).isEqualTo(1)
            assertThat(state.poets.first().imageUrl).isNotNull()
        }

    @Test
    fun `poet with no root categories is excluded from home list`() =
        runViewModelTest {
            val repository =
                FakePoetRepository().apply {
                    poetsWithRootCategories =
                        listOf(
                            PoetWithRootCategories(
                                poet =
                                    Poet(
                                        id = 1,
                                        name = "حافظ",
                                        description = null,
                                        rootCatId = 10,
                                        imageUrl = null,
                                    ),
                                rootCategories =
                                    listOf(
                                        CatNode(
                                            id = 100,
                                            poetId = 1,
                                            text = "غزلیات",
                                            parentId = 10,
                                            url = "/test",
                                        ),
                                    ),
                            ),
                            PoetWithRootCategories(
                                poet =
                                    Poet(
                                        id = 99,
                                        name = "شاعر بدون اثر",
                                        description = null,
                                        rootCatId = 500,
                                        imageUrl = null,
                                    ),
                                rootCategories = emptyList(),
                            ),
                        )
                }
            val viewModel = HomeViewModel(repository, FakeMemorizationRepository(), FakeDailyDistichRepository())

            val state = viewModel.state.value
            assertThat(state.poets.size).isEqualTo(1)
            assertThat(state.poets.first().id).isEqualTo(1)
        }

    @Test
    fun `load failure sets error state`() =
        runViewModelTest {
            val repository =
                FakePoetRepository().apply {
                    shouldFail = true
                }
            val viewModel = HomeViewModel(repository, FakeMemorizationRepository(), FakeDailyDistichRepository())

            assertThat(viewModel.state.value.screenState).isInstanceOf(UiScreenState.Error::class)
        }

    @Test
    fun `distich of day click emits poem detail navigation with today distich poem id`() =
        runViewModelTest {
            val dailyDistichRepository =
                FakeDailyDistichRepository().apply {
                    todayDistich = todayDistich.copy(poemId = 42)
                }
            val viewModel = HomeViewModel(FakePoetRepository(), FakeMemorizationRepository(), dailyDistichRepository)

            viewModel.onAction(HomeAction.OnDistichOfDayClick)

            viewModel.events.test {
                val event = awaitItem()
                assertThat(event).isEqualTo(HomeEvent.NavigateToPoemDetail(poemId = 42))
                cancelAndIgnoreRemainingEvents()
            }
        }
}
